package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.server.interfaces.WebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Stub class for implementing the queue of ThreadSafeQueue
 */
public class ThreadSafeQueue<T> {

    final static Logger logger = LogManager.getLogger(WebService.class);

    private Queue<T> queue;

    private boolean destory = false;

    public ThreadSafeQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized boolean queueTask(T task) {

        queue.add(task);
        notify();
        return true;
    }

    public synchronized void destroy() {
        destory = true;
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (!destory) {
            if (queue.isEmpty()) {
                wait();
            } else {
                T task = queue.poll();
                logger.info("Extracting new Task from the Queue", task);
                return task;
            }
        }
        if (destory) {
            if (queue.isEmpty()) {
                return null;
            } else {
                T task = queue.poll();
                logger.info("Extracting new Task from the Queue", task);
                return task;
            }
        }
        return null;
    }

    public synchronized T takeWithOutWait() throws InterruptedException {
        if (queue.isEmpty()) {
            return null;
        } else {
            T task = queue.poll();
            logger.info("Extracting new Task from the Queue", task);
            return task;
        }
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
