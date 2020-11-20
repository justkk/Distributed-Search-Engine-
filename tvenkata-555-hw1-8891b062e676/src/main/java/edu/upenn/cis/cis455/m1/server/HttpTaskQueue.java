package edu.upenn.cis.cis455.m1.server;


import edu.upenn.cis.cis455.m1.server.ThreadSafeQueue;

/***
 * A implementation of ThreadSafeQueue
 *
 * @param <T>
 *
 *     Just used here to maintain code structure.
 *     Any additional information can coded here.
 */
public class HttpTaskQueue<T> extends ThreadSafeQueue {

    public HttpTaskQueue() {
        super();
    }
}
