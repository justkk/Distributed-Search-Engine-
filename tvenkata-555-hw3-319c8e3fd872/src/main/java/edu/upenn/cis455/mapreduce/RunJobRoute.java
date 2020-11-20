package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.DistributedCluster;
import edu.upenn.cis.stormlite.distributed.WorkerJob;
import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStats;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

public class RunJobRoute implements Route {
    static Logger log = LogManager.getLogger(RunJobRoute.class);
    private DistributedCluster cluster;

    public RunJobRoute(DistributedCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        cluster = WorkerServer.getCluster();
        log.info("Starting job!");
        String jobId = request.queryParams("jobIndex");
        // TODO: start the topology on the DistributedCluster, which should start the dataflow

        if (WorkerServer.currentMachineWorker == null) {
            return "";
        }

        WorkerJob workerJob = WorkerServer.currentMachineWorker.getWorkerJobMap().get(jobId);
        if (workerJob == null) {
            return "";
        }

        synchronized (WorkerServer.currentMachineWorker) {
            if (!WorkerServer.currentMachineWorker.isThreadRunning()) {
                cluster.startTopology();
                WorkerServer.currentMachineWorker.setThreadRunning(true);
            }
        }

        return "Started";
    }

}
