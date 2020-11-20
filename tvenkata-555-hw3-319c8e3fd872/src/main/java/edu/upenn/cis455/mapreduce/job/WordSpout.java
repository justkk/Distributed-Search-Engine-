package edu.upenn.cis455.mapreduce.job;

import edu.upenn.cis.stormlite.spout.FileSpout;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class WordSpout extends FileSpout {


    @Override
    public String getFilename() {
        if (WorkerServer.currentMachineWorker == null || WorkerServer.currentMachineWorker.getCurrentJob() == null) {
            return "";
        }

        return WorkerServer.currentMachineWorker.getCurrentJob().getConfig().get("filePath");
    }
}
