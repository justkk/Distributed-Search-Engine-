package edu.upenn.cis.cis455.crawler.queue;

import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.cis455.redisHelper.RedisDBManager;
import edu.upenn.cis.cis455.redisHelper.RedisLockManager;
import edu.upenn.cis.cis455.storage.sqs.AWSQueueInterface;

import java.util.*;


/***
 *
 * This class is not thread safe. Implementor has to make it thread safe
 */
public class CrawlDataStructure {

    private String executorId = UUID.randomUUID().toString();

    Queue<ReadyQueueInstance> readyQueueInstances = new LinkedList<>();
    Queue<WaitingQueueInstance> waitingQueueInstances = new LinkedList<>();
    Set<ReadyQueueInstance> visitedReadyQueueInstanceSet = new HashSet<>();

    private RedisDBManager redisDBManager = new RedisDBManager(ConstantsHW2.getInstance().getRedisConfiguration());

    private AWSQueueInterface<ReadyQueueInstance> readyQueueInstanceAWSQueueInterface
            = new AWSQueueInterface<>("READY_QUEUE.fifo", ReadyQueueInstance.class);

    private AWSQueueInterface<WaitingQueueInstance> waitingQueueInstanceAWSQueueInterface
            = new AWSQueueInterface<>("WAIT_QUEUE.fifo", WaitingQueueInstance.class);

    private String VISITED_DOC_KEY_PREFIX = "####VISITED_DOC####";

    private String COUNT_DOC_KEY = "####DOC_COUNT####";

    int documentIndexed = 0;
    int documentLimit = 100;

    Map<String, Integer> runningTasks = new HashMap<>();

    public CrawlDataStructure(String startUrl, int documentLimit, int documentIndexed) {
        this.documentLimit = documentLimit;
        this.documentIndexed = documentIndexed;
        //this.readyQueueInstances.add(new ReadyQueueInstance(new URLInfo(startUrl)));
//        redisDBManager.incrBy("READY_COUNT", 1);
       addReadyQueueInstance(new ReadyQueueInstance(new URLInfo(startUrl)));
    }

    public int getQueueSize() {
//        System.out.println("Queue size: " + readyQueueInstances.size() + waitingQueueInstances.size() );
//        return readyQueueInstances.size() + waitingQueueInstances.size();
        return readyQueueInstanceAWSQueueInterface.queueSize() + waitingQueueInstanceAWSQueueInterface.queueSize();
    }

    public ReadyQueueInstance getReadyQueueInstance() {
//        if(readyQueueInstances.size() == 0) {
//            return null;
//        }
//        return readyQueueInstances.remove();

        ReadyQueueInstance readyQueueInstance =  readyQueueInstanceAWSQueueInterface.deQueue();
        if(readyQueueInstance!=null)
            System.out.println("Removing READY " +  readyQueueInstance.getUrlInfo().getUrl());
        return readyQueueInstance;
    }

    public Queue<ReadyQueueInstance> getReadyQueueInstances() {
        return readyQueueInstances;
    }

    public Queue<WaitingQueueInstance> getWaitingQueueInstances() {
        return waitingQueueInstances;
    }

    public List<WaitingQueueInstance> getWaitingQueueInstancesList(int size) {
        List<WaitingQueueInstance> ans =  waitingQueueInstanceAWSQueueInterface.deQueue(size);
        System.out.println("Removing WAIT " +  ans.size());
        return ans;
    }

    public void addWaitQueueInstance(WaitingQueueInstance waitingQueueInstance) {
        System.out.println(" Addding WAIT : " + waitingQueueInstance.getReadyQueueInstance().getUrlInfo().getUrl().toString());
        waitingQueueInstanceAWSQueueInterface.addToQueue(waitingQueueInstance);
    }

    public void addReadyQueueInstance(ReadyQueueInstance readyQueueInstance) {
        System.out.println(" Addding READY : " + readyQueueInstance.getUrlInfo().getUrl().toString());
        readyQueueInstanceAWSQueueInterface.addToQueue(readyQueueInstance);
    }

    public void addWaitQueueInstance(List<WaitingQueueInstance> waitingQueueInstanceList) {
        waitingQueueInstanceAWSQueueInterface.addToQueue(waitingQueueInstanceList);
    }

    public void addReadyQueueInstance(List<ReadyQueueInstance> readyQueueInstanceList) {
        readyQueueInstanceAWSQueueInterface.addToQueue(readyQueueInstanceList);
    }


    public void setWaitingQueueInstances(Queue<WaitingQueueInstance> waitingQueueInstances) {
        this.waitingQueueInstances = waitingQueueInstances;
    }

    public Set<ReadyQueueInstance> getVisitedReadyQueueInstanceSet() {
        return visitedReadyQueueInstanceSet;
    }


    public boolean addOnlyNotPresentReadyQueueInstanceSet(ReadyQueueInstance readyQueueInstance) {

        String redisKey = getReadyQueueInstanceKeyString(readyQueueInstance);
        String lockKey = "LOCK_" + redisKey;
        RedisLockManager redisLockManager =  redisDBManager.getLock(lockKey);
        String value = null;
        try {
            System.out.println("Taking Lock:  " + redisKey);
            redisLockManager.getJedisLock().acquire();
            value = redisDBManager.get(redisKey);
            if(value == null) {
                System.out.println("Adding to Visited Set" + redisKey );
                redisDBManager.setKey(redisKey, "1");
            }

        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            redisLockManager.getJedisLock().release();
            redisDBManager.returnResouce(redisLockManager);
            System.out.println("Lock released");

        }

        if(value == null) {
            return true;
        }
        return false;


    }

    public boolean addOnlyNotPresentReadyQueueInstances(ReadyQueueInstance readyQueueInstance) {

        String redisKey = getReadyQueueInstanceKeyString(readyQueueInstance);
        String lockKey = "LOCK_" + redisKey;
        RedisLockManager redisLockManager =  redisDBManager.getLock(lockKey);
        String value = null;
        try {
            System.out.println("Taking Lock " + redisKey);
            redisLockManager.getJedisLock().acquire();
            value = redisDBManager.get(redisKey);
            if(value == null) {
                System.out.println("Adding to Instance" + redisKey );
                //readyQueueInstances.add(readyQueueInstance);
                addReadyQueueInstance(readyQueueInstance);
            }

        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            redisLockManager.getJedisLock().release();
            redisDBManager.returnResouce(redisLockManager);
            System.out.println("Lock released");
        }

        if(value == null) {
            return true;
        }
        return false;


    }


    public void removeFromReadyQueueInstanceSet(ReadyQueueInstance readyQueueInstance) {
        String redisKey = getReadyQueueInstanceKeyString(readyQueueInstance);
        String lockKey = "LOCK_" + redisKey;
        RedisLockManager redisLockManager =  redisDBManager.getLock(lockKey);
        try {
            System.out.println("Taking Lock");
            redisLockManager.getJedisLock().acquire();
            System.out.println("Removing from visited set " + redisKey );
            redisDBManager.delete(redisKey);
            System.out.println("Removed from visited set" + redisKey);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("releasing lock");
            redisLockManager.getJedisLock().release();
            redisDBManager.returnResouce(redisLockManager);
            System.out.println("Lock released");
        }

    }

    public boolean canIndexNewDocument() {
        String value = redisDBManager.get(COUNT_DOC_KEY);
        if(value == null) {
            return true;
        }
        int cuurentcount = Integer.valueOf(redisDBManager.get(COUNT_DOC_KEY));
        return cuurentcount < this.documentLimit;
    }

    public void increamentCounter(int value) {

        redisDBManager.incrBy(COUNT_DOC_KEY, value);
        //this.documentIndexed += value;
    }

    public boolean isDocumentVisited(String hashValue) {

        String redisKey = VISITED_DOC_KEY_PREFIX + hashValue;
        return redisDBManager.get(redisKey) != null;
//        return false;
    }

    public void addDocumentVisited(String hashValue) {

        String redisKey = VISITED_DOC_KEY_PREFIX + hashValue;
        redisDBManager.setKey(redisKey, executorId);
    }


    public void addTask(TopoTask addTask, int count) {
        redisDBManager.incrBy("TASK_COUNT", count);
        //runningTasks.put(addTask.getId(), count);
    }

    public void removeTask(TopoTask topoTask) {
        redisDBManager.incrBy("TASK_COUNT", -1);
//        Integer count = runningTasks.get(topoTask.getId());
//        if(count > 1) {
//            runningTasks.put(topoTask.getId(), count-1);
//        } else {
//            runningTasks.remove(topoTask.getId());
//        }
    }

    public int getTaskSize() {
        String value = redisDBManager.get("TASK_COUNT");
        if(value == null) {
            return 0;
        }
        return Integer.valueOf(value);
       //return runningTasks.values().stream().mapToInt(i -> i.intValue()).sum();
    }

    public Map<String, Integer> getRunningTasks() {
        return runningTasks;
    }

    public String getReadyQueueInstanceKeyString(ReadyQueueInstance readyQueueInstance) {
        String redisKey = readyQueueInstance.getUrlInfo().getUrl().getProtocol() + "://" +
                readyQueueInstance.getUrlInfo().getHostName() + ":"
                + String.valueOf(readyQueueInstance.getUrlInfo().getPortNo())
                + readyQueueInstance.getUrlInfo().getFilePath();
        return redisKey;
    }
}





