package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.distributed.WorkerJob;

public class MasterJobStatus {

    private JobStatus jobStatus;
    private WorkerJob workerJob;

//    private int;

    public enum JobStatus {
        NOT_STARTED,
        RUNNING,
        END
    }
}
