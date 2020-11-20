package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

import java.util.*;

public class LinkExtractorBolt implements IRichBolt {

    private Crawler crawler;
    private CrawlerService crawlerService;
    private String executorId = UUID.randomUUID().toString();

    private Fields schema = new Fields();



    public LinkExtractorBolt(Crawler crawler) {
        this.crawler = crawler;
        this.crawlerService = crawler.getCrawlerService();
    }

    public LinkExtractorBolt() {
        this.crawler = Crawler.getCrawler();
        this.crawlerService = crawler.getCrawlerService();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

        Object obj = input.getObjectByField("crawlServiceResponse");

        CrawlServiceResponse crawlServiceResponse = (CrawlServiceResponse) ((TopoTask) obj).getObject();

        try {
            CrawlServiceResponse processedResponse = null;
            if (crawlServiceResponse.isProcessed()) {
                processedResponse = crawlServiceResponse;
            } else {
                processedResponse = crawlerService.processDocument(new ReadyQueueInstance(crawlServiceResponse.getProcessContext().getUrlInfo()),
                        crawlServiceResponse.getProcessContext());
            }
            if(processedResponse == null) {
                return;
            }
            if (processedResponse.isSuccess()) {

                List<ReadyQueueInstance> newTasksToBeInserted = new ArrayList<>();
                List<WaitingQueueInstance> newWaitsToBeInserted = new LinkedList<>();

                if (processedResponse.isSuccess()) {
                    newTasksToBeInserted = processedResponse.getReadyQueueInstanceList();
                    newWaitsToBeInserted = processedResponse.getWaitingQueueInstanceList();
                } else {
                }

                synchronized (crawler) {
                    int count = 0;
                    for (ReadyQueueInstance readyQueueInstanceTemp : newTasksToBeInserted) {
                        if (!crawler.getCrawlDataStructure().getReadyQueueInstanceSet().contains(readyQueueInstanceTemp)) {
                            crawler.getCrawlDataStructure().getReadyQueueInstances().add(readyQueueInstanceTemp);
                            count += 1;
                        }
                    }
                    for (WaitingQueueInstance waitingQueueInstance : newWaitsToBeInserted) {
                        crawler.getCrawlDataStructure().getReadyQueueInstanceSet().remove(waitingQueueInstance
                                .getReadyQueueInstance());
                        crawler.getCrawlDataStructure().getWaitingQueueInstances().add(waitingQueueInstance);
                    }

                }

            }
        } finally {
            synchronized (crawler) {
                crawler.getCrawlDataStructure().removeTask((TopoTask) obj);
            }
        }
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
    }

    @Override
    public void setRouter(IStreamRouter router) {
    }

    @Override
    public Fields getSchema() {
        return schema;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }
}
