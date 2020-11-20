package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CrawlerThread extends Thread {

    private Crawler crawler;
    private CrawlerThreadStatus crawlerThreadStatus;
    private CrawlerService crawlerService;

    private Logger logger = LogManager.getLogger(CrawlerThread.class);



    public CrawlerThread(Crawler crawler) {
        super();
        this.crawler = crawler;
        this.crawlerThreadStatus = CrawlerThreadStatus.IDLE;
        this.crawlerService = crawler.getCrawlerService();
    }

    public void processExit() {
        synchronized (crawler) {
            this.crawlerThreadStatus = CrawlerThreadStatus.TERMINATE;
        }
        crawler.processWorkerThreadExit(this);
    }

    public CrawlerThreadStatus getStatus() {
        return this.crawlerThreadStatus;
    }

    @Override
    public void run() {
        boolean newLife = true;
        while (true) {
            ReadyQueueInstance readyQueueInstance = null;
            synchronized (crawler) {
                while(true) {
                    try{
                        logger.debug("Checking indexed document count");
                        if(!crawler.getCrawlDataStructure().canIndexNewDocument()) {
                            logger.debug("cannot index any other document; cleaning queues and exit");
                            //crawler.getCrawlDataStructure().getReadyQueueInstances().clear();
                            //crawler.getCrawlDataStructure().getWaitingQueueInstances().clear();
                            crawler.notifyAll();
                            break;
                        }
                        if(crawler.shouldThreadTerminate()) {
                            logger.debug("Terminating thread");
                            crawler.notifyAll();
                            break;
                        }
                        readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                        while(readyQueueInstance!=null &&
                                !crawler.getCrawlDataStructure().addOnlyNotPresentReadyQueueInstanceSet(readyQueueInstance)) {
                            logger.debug("Already Seen This Task, checking other one");
                            readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                        }

                        if(readyQueueInstance == null) {
                            logger.debug("Waiting for task");
                            crawler.wait();
                        } else {
                            logger.debug("Found a new task "+ readyQueueInstance.getUrlInfo().getUrl());
//                            crawler.getCrawlDataStructure().addToReadyQueueInstanceSet(readyQueueInstance);
                            this.crawlerThreadStatus = CrawlerThreadStatus.RUNNING;
                            break;
                        }
                    } catch (InterruptedException e) {
                        //logger.error("Error " + e.getMessage());
                    }
                }
            }

            if(readyQueueInstance == null) {
                processExit();
                return;
            }

            logger.debug("Working.... on Task ");
            logger.debug(readyQueueInstance.getUrlInfo().getFilePath());
            CrawlServiceResponse crawlServiceResponse = null;
            List<ReadyQueueInstance> newTasksToBeInserted = new ArrayList<>();
            List<WaitingQueueInstance> newWaitsToBeInserted = new LinkedList<>();

            try {
                crawlServiceResponse = crawlerService.processRequest(readyQueueInstance);
                if(crawlServiceResponse!=null && crawlServiceResponse.isSuccess()) {
//                    System.out.println(crawlServiceResponse.getReadyQueueInstanceList().size() + " "
//                            + crawlServiceResponse.getWaitingQueueInstanceList().size());
                    newTasksToBeInserted = crawlServiceResponse.getReadyQueueInstanceList();
                    newWaitsToBeInserted = crawlServiceResponse.getWaitingQueueInstanceList();
                } else {
                    logger.debug("Task Failed" + crawlServiceResponse.getMessage());
                }
            } catch (Exception e) {
                logger.debug(e.getMessage());
            } catch (Error e) {
                logger.debug(e.getMessage());
            }
            catch (Throwable throwable) {
                logger.debug(throwable.getMessage());
            }

            logger.debug("Done Task");



            synchronized (crawler) {
                // add new tasks
//                System.out.println("Ready Queue: " + crawler.getCrawlDataStructure().getReadyQueueInstances().size() );
//                System.out.println("Wait Queue: " + crawler.getCrawlDataStructure().getWaitingQueueInstances().size() );
                this.crawlerThreadStatus = CrawlerThreadStatus.IDLE;
                int count = 0;
                for(ReadyQueueInstance readyQueueInstanceTemp : newTasksToBeInserted) {
                    if(crawler.getCrawlDataStructure().addOnlyNotPresentReadyQueueInstanceSet(readyQueueInstanceTemp)) {
                        //crawler.getCrawlDataStructure().addToReadyQueueInstanceSet(readyQueueInstanceTemp);
                        count += 1;
                    }
                }
                logger.debug("new tasks added " + count);
                for(WaitingQueueInstance waitingQueueInstance : newWaitsToBeInserted) {
                    crawler.getCrawlDataStructure().removeFromReadyQueueInstanceSet(waitingQueueInstance
                            .getReadyQueueInstance());
                    crawler.getCrawlDataStructure().addWaitQueueInstance(waitingQueueInstance);
                }
                crawler.balanceThreads();
                for(int i=0; i < count; i++) {
                    crawler.notify();
                }
            }
        }

    }

    public CrawlerThreadStatus getCrawlerThreadStatus() {
        return crawlerThreadStatus;
    }

    public enum CrawlerThreadStatus {
        RUNNING,
        TERMINATE,
        IDLE;
    }


}
