package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStatsEnum;
import edu.upenn.cis455.mapreduce.pojo.ResultPojo;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class CollectorWrapper implements Context {

    private OutputCollector outputCollector;
    private WorkerServer workerServer;

    public CollectorWrapper(OutputCollector outputCollector, WorkerServer workerServer) {
        this.outputCollector = outputCollector;
        this.workerServer = workerServer;
    }

    @Override
    public void write(String key, String value, String sourceExecutor) {
        outputCollector.write(key, value, sourceExecutor);
        synchronized (workerServer) {
            if (workerServer.getCurrentWorkerStats().getCurrentWorkerStatsEnum() == CurrentWorkerStatsEnum.MAP_PHASE) {
                synchronized (workerServer) {
                    workerServer.getCurrentWorkerStats().incMapWrite();
                }
            }
            if (workerServer.getCurrentWorkerStats().getCurrentWorkerStatsEnum() == CurrentWorkerStatsEnum.REDUCE_PHASE) {
                synchronized (workerServer) {
                    workerServer.getCurrentWorkerStats().incReduceWrite();
                    if (workerServer.getCurrentWorkerStats().getSampleOutput().size() < 100) {
                        workerServer.getCurrentWorkerStats().getSampleOutput().add(new ResultPojo(key, value, sourceExecutor));
                    }
                }
            }
        }
    }

}
