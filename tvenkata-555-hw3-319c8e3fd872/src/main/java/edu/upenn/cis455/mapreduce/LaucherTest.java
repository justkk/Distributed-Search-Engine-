package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis455.mapreduce.master.MasterServer;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class LaucherTest {

    public static void launchWokers(Config config) {
        WorkerServer.createWorker(config);
    }

    public static void launchMaster(Config config) {
        String masterIp = config.get("masterHost");
    }

    public static void main(String[] args) {

        Constants constants = Constants.getInstance();

        Config config = new Config();
        Utils.addMap(config, constants.getNetworkConfig());
        int workerCount = Integer.valueOf(config.get("workersLength"));

    }
}
