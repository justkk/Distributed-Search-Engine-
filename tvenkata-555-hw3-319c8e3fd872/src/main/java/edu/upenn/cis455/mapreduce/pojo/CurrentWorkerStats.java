package edu.upenn.cis455.mapreduce.pojo;

import edu.upenn.cis.stormlite.distributed.WorkerJob;

import java.util.ArrayList;

public class CurrentWorkerStats {

    private WorkerJob job;

    private int workerIndex;

    private CurrentWorkerStatsEnum currentWorkerStatsEnum;
    private ArrayList<ResultPojo> sampleOutput = new ArrayList<>();


    private int mapCount = 0;
    private int reduceCount = 0;
    private int printCount = 0;

    private int reduceLimitCount = 0;
    private int reduceCompleteCount = 0;
    private int reduceStartCount = 0;

    private int mapKeyReadCount = 0;
    private int mapKeyWriteCount = 0;
    private int reduceKeyReadCount = 0;
    private int reduceKeyWriteCount = 0;

    public CurrentWorkerStats(WorkerJob workerJob) {
        currentWorkerStatsEnum = CurrentWorkerStatsEnum.IDLE_PHASE;
        this.workerIndex = Integer.valueOf(workerJob.getConfig().get("workerIndex"));
        this.job = workerJob;
        this.reduceLimitCount = Integer.valueOf(workerJob.getConfig().get("reduceExecutors"));
    }

    public CurrentWorkerStats() {
        currentWorkerStatsEnum = CurrentWorkerStatsEnum.IDLE_PHASE;
        this.workerIndex = -1;
        this.job = null;
    }


//    public void changeState(CurrentJobWorkerStatsEnum currentJobWorkerStatsEnum) {
//        if(currentJobWorkerStatsEnum == CurrentJobWorkerStatsEnum.MAP_PHASE
//                || currentJobWorkerStatsEnum == CurrentJobWorkerStatsEnum.REDUCE_PHASE) {
//
//        }
//        this.currentJobWorkerStatsEnum = currentJobWorkerStatsEnum;
//    }

    public void startingVote(int value, CurrentWorkerStatsEnum currentWorkerStatsEnum) {
        switch (currentWorkerStatsEnum) {
            case MAP_PHASE:
                mapCount += value;
                break;
            case REDUCE_PHASE:
                reduceStartCount += value;
                reduceCount += value;
                break;
        }
        decisionEngine();
    }

    public void endingVote(int value, CurrentWorkerStatsEnum currentWorkerStatsEnum) {

        switch (currentWorkerStatsEnum) {
            case MAP_PHASE:
                mapCount -= value;
                break;
            case REDUCE_PHASE:
                reduceCount -= value;
                reduceCompleteCount += value;
                break;
        }
        decisionEngine();
    }

    public synchronized void incMapRead() {
        mapKeyReadCount += 1;
    }

    public void incMapWrite() {
        mapKeyWriteCount += 1;
    }

    public synchronized void incReduceRead() {
        reduceKeyReadCount += 1;
    }

    public void incReduceWrite() {
        reduceKeyWriteCount += 1;
    }

    public void decisionEngine() {

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.IDLE_PHASE && mapCount > 0) {
            currentWorkerStatsEnum = CurrentWorkerStatsEnum.MAP_PHASE;
            sampleOutput.clear();
        } else if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.MAP_PHASE && mapCount == 0) {
            currentWorkerStatsEnum = CurrentWorkerStatsEnum.WAITING_PHASE;
        } else if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.WAITING_PHASE && reduceStartCount > 0) {
            currentWorkerStatsEnum = CurrentWorkerStatsEnum.REDUCE_PHASE;
        } else if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.REDUCE_PHASE && reduceCompleteCount == reduceLimitCount && workerIndex!=0) {
            currentWorkerStatsEnum = CurrentWorkerStatsEnum.IDLE_PHASE;
        } else if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.REDUCE_PHASE && reduceCompleteCount == reduceLimitCount && workerIndex==0) {
            currentWorkerStatsEnum = CurrentWorkerStatsEnum.PRINT_PHASE;
        }
    }

    public void printDone() {
        currentWorkerStatsEnum = CurrentWorkerStatsEnum.IDLE_PHASE;
    }

    public ArrayList<ResultPojo> getSampleOutput() {
        return sampleOutput;
    }

    public CurrentWorkerStatsEnum getCurrentWorkerStatsEnum() {
        return currentWorkerStatsEnum;
    }

    public int getMapCount() {
        return mapCount;
    }

    public int getReduceCount() {
        return reduceCount;
    }

    public int getPrintCount() {
        return printCount;
    }

    public int getMapKeyReadCount() {
        return mapKeyReadCount;
    }

    public int getMapKeyWriteCount() {
        return mapKeyWriteCount;
    }

    public int getReduceKeyReadCount() {
        return reduceKeyReadCount;
    }

    public int getReduceKeyWriteCount() {
        return reduceKeyWriteCount;
    }

    public int getKeysRead() {

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.IDLE_PHASE
                || currentWorkerStatsEnum == CurrentWorkerStatsEnum.PRINT_PHASE) {
            return 0;
        }

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.MAP_PHASE) {
            return mapKeyReadCount;
        }

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.REDUCE_PHASE) {
            return reduceKeyReadCount + mapKeyReadCount;
        }

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.WAITING_PHASE) {
            return mapKeyReadCount;
        }

        return -1;
    }

    public int getKeysWritten() {

        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.IDLE_PHASE
                || currentWorkerStatsEnum == CurrentWorkerStatsEnum.PRINT_PHASE) {
            return reduceKeyWriteCount + mapKeyWriteCount;
        }
        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.MAP_PHASE) {
            return mapKeyWriteCount;
        }
        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.REDUCE_PHASE) {
            return reduceKeyWriteCount + mapKeyWriteCount;
        }
        if (currentWorkerStatsEnum == CurrentWorkerStatsEnum.WAITING_PHASE) {
            return mapKeyWriteCount;
        }
        return -1;
    }

    public WorkerJob getJob() {
        return job;
    }
}
