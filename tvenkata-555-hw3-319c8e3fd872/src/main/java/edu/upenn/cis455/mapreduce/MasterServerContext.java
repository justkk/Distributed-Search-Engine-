package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.distributed.WorkerJob;

import java.util.HashMap;
import java.util.Map;

public class MasterServerContext {

    private Config config;

    private WorkerJob lastWorkingJob;

    public MasterServerContext(Map<String, String> networkConfig) {
        this.config = new Config();
        Utils.addMap(config, networkConfig);
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setLastWorkingJob(WorkerJob lastWorkingJob) {
        this.lastWorkingJob = lastWorkingJob;
    }



    //    public synchronized boolean addJob(WorkerJob workerJob) {
//        if (workerJobs.size() < Constants.getInstance().getBlockingQueueSize()) {
//            workerJobs.add(workerJob);
//            return true;
//        }
//        return false;
//    }
//
//    public synchronized WorkerJob fetchJob() {
//        if (workerJobs.size() == 0) {
//            return null;
//        }
//        return workerJobs.peekFirst();
//    }
//
//    public synchronized void removeJob() {
//        if (workerJobs.size() != 0) {
//            workerJobs.removeFirst();
//        }
//    }
}
