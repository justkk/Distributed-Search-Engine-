package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class CrawlerQueueSpout implements IRichSpout {

    static Logger log = LogManager.getLogger(CrawlerQueueSpout.class);

    private String executorId = UUID.randomUUID().toString();

    private SpoutOutputCollector collector;


    private Crawler crawler;

    public CrawlerQueueSpout(Crawler crawler) {
        this.crawler = crawler;
    }

    public CrawlerQueueSpout() {
        this.crawler = Crawler.getCrawler();
    }

    @Override
    public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void close() {
    }

    @Override
    public void nextTuple() {
        ReadyQueueInstance readyQueueInstance = null;
        //while (true) {

        try{

            synchronized (crawler) {
                try {
                    //logger.debug("Checking indexed document count");
//                    if(!crawler.getCrawlDataStructure().canIndexNewDocument()) {
//                        //logger.debug("cannot index any other document; cleaning queues and exit");
//                        crawler.getCrawlDataStructure().getReadyQueueInstances().clear();
//                        crawler.getCrawlDataStructure().getWaitingQueueInstances().clear();
//                        crawler.notifyAll();
//                        break;
//                    }
//                    if(crawler.shouldThreadTerminate()) {
//                        //logger.debug("Terminating thread");
//                        crawler.notifyAll();
//                        break;
//                    }
                    readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                    while (readyQueueInstance != null &&
                            crawler.getCrawlDataStructure().getReadyQueueInstanceSet()
                                    .contains(readyQueueInstance)) {
                        //logger.debug("Already Seen This Task, checking other one");
                        readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                    }

                    if (readyQueueInstance == null) {
                        //logger.debug("Waiting for task");
                        //crawler.wait();
                        //Thread.yield();
                    } else {
                        //logger.debug("Found a new task "+ readyQueueInstance.getUrlInfo().getUrl());
                        crawler.getCrawlDataStructure().getReadyQueueInstanceSet().add(readyQueueInstance);
                        //this.crawlerThreadStatus = CrawlerThread.CrawlerThreadStatus.RUNNING;
                        //break;
                    }
                } catch (Exception e) {
                    //logger.error("Error " + e.getMessage());
                }
            }
            if(readyQueueInstance!=null) {
                TopoTask topoTask = new TopoTask(readyQueueInstance);
                synchronized (crawler) {
                    crawler.getCrawlDataStructure().addTask(topoTask, 1);
                }
                this.collector.emit(new Values<Object>(topoTask));
            }
            Thread.yield();
        } finally {
            // nothing as of now.
        }



        //}
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("readyQueueInstance"));
    }
}
