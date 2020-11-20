package edu.upenn.cis.cis455.crawler.queue;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.ms2.model.TopoTask;

import java.util.*;


/***
 *
 * This class is not thread safe. Implementor has to make it thread safe
 */
public class CrawlDataStructure {

    Queue<ReadyQueueInstance> readyQueueInstances = new LinkedList<>();
    Queue<WaitingQueueInstance> waitingQueueInstances = new LinkedList<>();
    Set<ReadyQueueInstance> readyQueueInstanceSet = new HashSet<>();
    Set<String> visitedHashDocumentSet = new HashSet<>();
    Map<String, String> visitedHashDocumentUrlMap = new HashMap<>();

    int documentIndexed = 0;
    int documentLimit = 100;

    Map<String, Integer> runningTasks = new HashMap<>();

    public CrawlDataStructure(String startUrl, int documentLimit, int documentIndexed) {
        this.documentLimit = documentLimit;
        this.documentIndexed = documentIndexed;
        this.readyQueueInstances.add(new ReadyQueueInstance(new URLInfo(startUrl)));
    }

    public int getQueueSize() {
//        System.out.println("Queue size: " + readyQueueInstances.size() + waitingQueueInstances.size() );
        return readyQueueInstances.size() + waitingQueueInstances.size();
    }

    public ReadyQueueInstance getReadyQueueInstance() {
        if(readyQueueInstances.size() == 0) {
            return null;
        }
        return readyQueueInstances.remove();
    }

    public Queue<ReadyQueueInstance> getReadyQueueInstances() {
        return readyQueueInstances;
    }

    public Queue<WaitingQueueInstance> getWaitingQueueInstances() {
        return waitingQueueInstances;
    }

    public void setWaitingQueueInstances(Queue<WaitingQueueInstance> waitingQueueInstances) {
        this.waitingQueueInstances = waitingQueueInstances;
    }

    public Set<ReadyQueueInstance> getReadyQueueInstanceSet() {
        return readyQueueInstanceSet;
    }

    public boolean canIndexNewDocument() {
        return this.documentIndexed < this.documentLimit;
    }

    public void increamentCounter(int value) {
        this.documentIndexed += value;
    }

    public int getDocumentIndexed() {
        return documentIndexed;
    }

    public Set<String> getVisitedHashDocumentSet() {
        return visitedHashDocumentSet;
    }

    public void setVisitedHashDocumentSet(Set<String> visitedHashDocumentSet) {
        this.visitedHashDocumentSet = visitedHashDocumentSet;
    }

    public Map<String, String> getVisitedHashDocumentUrlMap() {
        return visitedHashDocumentUrlMap;
    }

    public void addTask(TopoTask addTask, int count) {
        runningTasks.put(addTask.getId(), count);
    }

    public void removeTask(TopoTask topoTask) {
        Integer count = runningTasks.get(topoTask.getId());
        if(count > 1) {
            runningTasks.put(topoTask.getId(), count-1);
        } else {
            runningTasks.remove(topoTask.getId());
        }
    }

    public int getTaskSize() {
       return runningTasks.values().stream().mapToInt(i -> i.intValue()).sum();
    }

    public Map<String, Integer> getRunningTasks() {
        return runningTasks;
    }
}





