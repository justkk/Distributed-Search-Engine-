package edu.upenn.cis455.mapreduce.master;

import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStatsEnum;
import edu.upenn.cis455.mapreduce.restModels.WorkerStatus;

import java.util.*;

public class MasterCleaner implements Runnable{

    private MasterServer masterServer;

    public MasterCleaner(MasterServer masterServer) {
        this.masterServer = masterServer;
    }

    public void cleanDeadWorkers() {
        synchronized (masterServer) {
            Date currentDate = new Date();
            Set<String> idList = new HashSet<>();
            for(Map.Entry<String, WorkerStatus> statusEntry : masterServer.getWorkerStatusLinkedHashMap().entrySet()) {
                if(currentDate.getTime() - statusEntry.getValue().getRequestDate().getTime() > 30000) {
                    idList.add(statusEntry.getKey());
                }
            }

            for (String id: idList) {
                masterServer.getWorkerStatusLinkedHashMap().remove(id);
            }
        }
    }

    public void updateRunningState() {

        if(masterServer.getCurrentWorkingJob() == null) {
            return;
        }

        String jobId = masterServer.getCurrentWorkingJob().getConfig().get("jobId");
        List<String> workerIp = masterServer.getWorkIpMapping().get(jobId);
        if(workerIp == null) {
            return;
        }
        boolean isDone = true;

        synchronized (masterServer) {

            for(String work : workerIp) {
                WorkerStatus workerStatus = masterServer.getWorkerStatusLinkedHashMap().getOrDefault(work, null);
                if(workerStatus == null) {
                    // worker dead
                    continue;
                }
                if(workerStatus.getCurrentWorkerStatsEnum() != CurrentWorkerStatsEnum.IDLE_PHASE) {
                    isDone = false;
                    break;
                }
            }
            if(!isDone) {
                return;
            }

            masterServer.setJobRunning(false);
            masterServer.setCurrentWorkingJob(null);

        }



    }


    @Override
    public void run() {
        while (true) {
            synchronized (masterServer) {
                if(!masterServer.isActive()) {
                    break;
                }
                cleanDeadWorkers();
                updateRunningState();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
