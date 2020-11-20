package edu.upenn.cis.cis455.m1.server.genericImpl;

import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ThreadExecutorManager;
import edu.upenn.cis.cis455.m1.server.ThreadSafeQueue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/***
 *
 * A thread executor used by server.
 */

public class InHouseThreadExecutor {

    /***
     * State var
     */
    private ThreadExecutorState threadExecutorState;
    /***
     * ThreadExecutorManager to hook start and end of processing.
     */
    private ThreadExecutorManager threadExecutorManager;

    private long completedTaskCount;

    public void setCompletedTaskCount(long change) {
        this.completedTaskCount = this.completedTaskCount + change;
    }

    private boolean isRunning() {
        return this.threadExecutorState == ThreadExecutorState.RUNNING;
    }

    private boolean isStop() {

        return this.threadExecutorState == ThreadExecutorState.STOP;
    }

    private boolean isShutdown() {

        return this.threadExecutorState == ThreadExecutorState.SHUTDOWN;
    }

    private boolean isTerminate() {

        return this.threadExecutorState == ThreadExecutorState.TERMINATED;
    }

    /**
     * Task Queue
     */


    private final ThreadSafeQueue<Runnable> workQueue;

    /***
     * Workers set.
     */
    private Set<Worker> workers;

    private final int threadPoolSize;

    public ThreadSafeQueue<Runnable> getWorkQueue() {
        return workQueue;
    }

    public InHouseThreadExecutor(int threadPoolSize, ThreadExecutorManager threadExecutorManager, ThreadSafeQueue<Runnable> threadSafeQueue) {
        this.threadPoolSize = threadPoolSize;
        this.workQueue = threadSafeQueue;
        this.workers = new HashSet<>();
        this.threadExecutorState = null;
        this.threadExecutorManager = threadExecutorManager;
    }

    public void start() {
        synchronized (this) {
            this.threadExecutorState = ThreadExecutorState.RUNNING;
        }
    }

    public void addTask(Runnable task) {

        if (isRunning()) {
            /***
             * if workers count is less, create new workers
             */
            boolean isWorkedAdded = addWorker(task);
            if (!isWorkedAdded) {
                /***
                 * else add it to queue.
                 */
                workQueue.queueTask(task);
            }
        }
    }

    /**
     * Fetch task from queue. It has to be Runnable.
     *
     */

    private Runnable getTask() {
        for (; ; ) {
            try {
                if (!isRunning() && workQueue.isEmpty()) {
                    //decrementCounter();
                    return null;
                }
                Runnable task = isRunning() ? workQueue.take() : workQueue.takeWithOutWait();
                return task;
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    /***
     * Tries creating new worker
     * @param firstTask : first task for the created worker
     * @return true if worker is created. False if we have reached max worker capacity.
     */

    private boolean addWorker(Runnable firstTask) {

        if (!isRunning() && workQueue.isEmpty())
            return false;

        if (isStop()) {
            return false;
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;

        try {
            synchronized (this) {

                if (this.workers.size() == threadPoolSize) {
                    return false;
                }

                w = new Worker(firstTask, this);
                final Thread t = w.thread;
                if (t != null) {
                    try {
                        if (t.isAlive())
                            throw new IllegalStateException();
                        workers.add(w);
                        workerAdded = true;
                    } finally {
                        if (workerAdded) {
                            t.start();
                            workerStarted = true;
                        }
                    }
                }
            }
        } finally {
            if (!workerStarted)
                addWorkerFailed(w);

        }
        return workerStarted;
    }

    public void interruptThreads() {

        for (Worker w : workers) {
            Thread t = w.thread;
            try {
                t.interrupt();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    /***
     * Shutdown the executor and workers.
     */

    public void shutdown() {

        synchronized (this) {
            this.threadExecutorState = ThreadExecutorState.SHUTDOWN;
            //interruptThreads();
        }
        this.workQueue.destroy();
        tryTerminate();
    }

    private void addWorkerFailed(Worker w) {

        synchronized (this) {
            if (w != null && workers.contains(w)) {
                workers.remove(w);
                tryTerminate();
            }
        }
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly) {

        synchronized (w.executor) {
            w.executor.setCompletedTaskCount(w.completedTasks);
            w.executor.workers.remove(w);
        }
    }

    private void beforeExecute(Worker worker, Runnable task) {
        if (threadExecutorManager != null) {
            threadExecutorManager.start(worker, task);
        }
    }

    ;

    private void afterExecute(Worker worker, Runnable task, Throwable thrown) {
        if (threadExecutorManager != null) {

            if (thrown == null) {
                threadExecutorManager.done(worker, task);
            } else {
                threadExecutorManager.error(worker, task, thrown);
            }
        }

    }

    /***
     * Start the worker.
     * @param w
     */

    private void runWorker(Worker w) {

        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        boolean completedAbruptly = true;
        w.currentTask = task;
        try {
            while (task != null || (task = getTask()) != null) {

                w.currentTask = task;
                w.setIntoRunningState();

                if ((isStop() || (Thread.interrupted() && isStop())) && !wt.isInterrupted())
                    wt.interrupt();

                try {
                    Throwable thrown = null;
                    try {
                        beforeExecute(w, task);
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x;
                        System.out.println("Error");
                        //throw x;
                    } catch (Error x) {
                        thrown = x;
                        System.out.println("Error");
                        //throw x;
                    } catch (Throwable x) {
                        thrown = x;
                        System.out.println("Error");
                        //throw new Error(x);
                    } finally {
                        if (thrown == null) {
                        }
                        afterExecute(w, task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.setIntoIdleState();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    /**
     * Try to terminate the executor
     */

    final void tryTerminate() {

        for (; ; ) {

            if (isRunning() ||
                    (isShutdown() && !workQueue.isEmpty()))
                return;

            if (threadExecutorState == ThreadExecutorState.TERMINATED) {
                return;
            }

            if (workers.size() == 0) {
                synchronized (this) {
                    threadExecutorState = ThreadExecutorState.TERMINATED;
                    System.out.println("workers dead");
                    break;
                }
            }

            return;
        }


    }

    /***
     * Wait till the workers are terminated.
     */

    public void waitForTermination() {
        for (; ; ) {

            if(workQueue.isEmpty() && workers.size() == 0) {
                break;
            }

            if (isTerminate()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    /***
     * Get the worker stats.
     * @return
     */

    public Map<String, String> getWorkerStats() {
        Map<String, String> stats = new HashMap<>();
        for (Worker worker : workers) {
            stats.put(worker.name, worker.getWorkerState().toString());
        }
        return stats;
    }


    public class Worker implements Runnable {

        private static final long serialVersionUID = 6138294804551838833L;

        final Thread thread;

        Runnable firstTask;

        volatile long completedTasks;

        private final InHouseThreadExecutor executor;

        private volatile WorkerState workerState;

        private Runnable currentTask;

        public Runnable getCurrentTask() {
            return currentTask;
        }

        private String name;

        public String getName() {
            return name;
        }

        public WorkerState getWorkerState() {
            return workerState;
        }

        public synchronized void setIntoRunningState() {
            workerState = WorkerState.RUNNING;
        }

        public synchronized void setIntoIdleState() {
            workerState = WorkerState.IDLE;
        }


        public Worker(Runnable firstTask, InHouseThreadExecutor threadExecutor) {
            setIntoIdleState();
            this.firstTask = firstTask;
            this.thread = new Thread(this);
            this.executor = threadExecutor;
            this.name = thread.getName();
        }


        @Override
        public void run() {
            runWorker(this);
        }


    }

    /***
     * Enum Executor State.
     */

    public enum ThreadExecutorState {
        RUNNING,
        SHUTDOWN,
        STOP,
        TERMINATED;
    }

    /***
     * Enum WorkerState .
     */

    public enum WorkerState {
        RUNNING,
        IDLE;
    }

    public static void main(String[] args) throws InterruptedException {
        InHouseThreadExecutor inHouseThreadExecutor = new InHouseThreadExecutor(3, null, new ThreadSafeQueue<>());
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        inHouseThreadExecutor.start();
        inHouseThreadExecutor.addTask(task);
        inHouseThreadExecutor.addTask(task);
        inHouseThreadExecutor.addTask(task);
        inHouseThreadExecutor.addTask(task);


        Thread.sleep(5000);
        System.out.println("Main Exited");
        inHouseThreadExecutor.shutdown();
        System.out.println("Main Exited");
    }
}
