package edu.upenn.cis.cis455.m1.server.genericImpl.interfaces;

import edu.upenn.cis.cis455.m1.server.genericImpl.InHouseThreadExecutor;

public interface ThreadExecutorManager {

    /**
     * Get access to the request queue, to
     * poll, sleep, etc.
     */
    /**
     * Is the Web server still active, or
     * do we need to shut down?
     */

    /**
     * Tell the coordinator we have started
     * a task
     */
    public void start(InHouseThreadExecutor.Worker worker, Runnable task);

    /**
     * Tell the coordinator we have completed
     * a task
     */
    public void done(InHouseThreadExecutor.Worker worker, Runnable task);

    /**
     * Tell the coordinator we failed a task
     */
    public void error(InHouseThreadExecutor.Worker worker, Runnable task, Throwable e);

}
