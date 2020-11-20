package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.validator.processImpl.RobotPermissionChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CrawlerCleaner extends Thread {

    private Crawler crawler;
    private CrawlerThread.CrawlerThreadStatus crawlerThreadStatus;
    private int cleanerWaitTime;
    private int dequeueSize = 10;

    private Logger logger = LogManager.getLogger(CrawlerCleaner.class);

    public CrawlerCleaner(Crawler crawler, int cleanerWaitTime) {
        super();
        this.crawler = crawler;
        this.crawlerThreadStatus = CrawlerThread.CrawlerThreadStatus.RUNNING;
        this.cleanerWaitTime = cleanerWaitTime;
    }

    public void processExit() {
        this.crawlerThreadStatus = CrawlerThread.CrawlerThreadStatus.TERMINATE;
        crawler.shutDownV2(false, false);
    }

    public boolean shouldWait(WaitingQueueInstance waitingQueueInstance) {

        Date prevDate = waitingQueueInstance.getInsertedTime();
        Date currentDate = new Date();
        if(currentDate.getTime() - prevDate.getTime() >= waitingQueueInstance.getWaitingTime()) {
            return false;
        }
        return true;

    }

    @Override
    public void run() {


        while (true) {

            System.out.println("Running Cleaner ...... ");

            if(crawler.getStatus() == Crawler.CrawlManagerStatus.WAITING_FOR_TERMINATE ||
                    crawler.getStatus() == Crawler.CrawlManagerStatus.TERMINATE) {
                logger.debug("Clear Exit");
                break;
            }
            System.out.println("Waiting for crawler lock");
            synchronized (crawler) {

                System.out.println("Checking indexed document count");
//                if(!crawler.getCrawlDataStructure().canIndexNewDocument()) {
//                    logger.debug("cannot index any other document; cleaning queues and exit");
//                    crawler.getCrawlDataStructure().getReadyQueueInstances().clear();
//                    crawler.getCrawlDataStructure().getWaitingQueueInstances().clear();
//                    crawler.notifyAll();
//                    break;
//                }
//                if(crawler.shouldThreadTerminateV2()) {
//                    logger.debug("Terminating thread");
//                    crawler.notifyAll();
//                    break;
//                }


                this.crawlerThreadStatus = CrawlerThread.CrawlerThreadStatus.RUNNING;
                System.out.println("Checking waiting instances");

                List<WaitingQueueInstance> fetchedInstances = new ArrayList<>();
                List<WaitingQueueInstance> needToWait = new LinkedList<>();
                List<ReadyQueueInstance> makeItReady = new ArrayList<>();

                int iterCount = 0;


                while (true) {

                    fetchedInstances = crawler.getCrawlDataStructure().getWaitingQueueInstancesList(dequeueSize);

                    if(fetchedInstances == null || fetchedInstances.size() == 0 || iterCount > 10) {
                        break;
                    }

                    for(WaitingQueueInstance waitingQueueInstance: fetchedInstances) {
                        if(shouldWait(waitingQueueInstance)) {
                            needToWait.add(waitingQueueInstance);
                        //crawler.getCrawlDataStructure().addWaitQueueInstance(waitingQueueInstance);
                        } else {
                            //crawler.getCrawlDataStructure().addReadyQueueInstance(waitingQueueInstance.getReadyQueueInstance());
                        makeItReady.add(waitingQueueInstance.getReadyQueueInstance());
                        }
                    }

                    System.out.println("Transferring " + makeItReady.size());
                    iterCount += 1;


                }

                crawler.getCrawlDataStructure().addWaitQueueInstance(needToWait);
//                //crawler.getCrawlDataStructure().getReadyQueueInstances().addAll(makeItReady);
                crawler.getCrawlDataStructure().addReadyQueueInstance(makeItReady);
//                for (int i =0; i<makeItReady.size();i++) {
//                    crawler.notify();
//                }

                this.crawlerThreadStatus = CrawlerThread.CrawlerThreadStatus.IDLE;
            }


            try {
                Thread.sleep(this.cleanerWaitTime);
            } catch (InterruptedException e) {
                //logger.error("Error" + e.getMessage());
            }
        }
        //processExit();
    }

    public CrawlerThread.CrawlerThreadStatus getCrawlerThreadStatus() {
        return crawlerThreadStatus;
    }
}
