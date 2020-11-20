package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

import java.util.Map;
import java.util.UUID;

public class DocumentFetcherBolt implements IRichBolt {

    private Crawler crawler;
    private CrawlerService crawlerService;

    private Fields schema = new Fields("crawlServiceResponse");

    private OutputCollector collector;

    private String executorId = UUID.randomUUID().toString();

    public DocumentFetcherBolt(Crawler crawler) {
        this.crawler = crawler;
        this.crawlerService = crawler.getCrawlerService();
    }

    public DocumentFetcherBolt() {
        this.crawler = Crawler.getCrawler();
        this.crawlerService = crawler.getCrawlerService();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

        Object obj = input.getObjectByField("readyQueueInstance");
        ReadyQueueInstance readyQueueInstance = (ReadyQueueInstance) ((TopoTask) obj).getObject();
        if (readyQueueInstance == null) {
            return;
        }
        try {
            CrawlServiceResponse crawlServiceResponse = null;
            try {
                crawlServiceResponse = crawlerService.processRequest(readyQueueInstance);
            } catch (Exception e) {
                //logger.debug(e.getMessage());
            } catch (Error e) {
                //logger.debug(e.getMessage());
            } catch (Throwable throwable) {
                //logger.debug(throwable.getMessage());
            }
            if(crawlServiceResponse != null) {
                TopoTask topoTask = new TopoTask(crawlServiceResponse);
                synchronized (crawler) {
                    crawler.getCrawlDataStructure().addTask(topoTask, 2);
                }
                collector.emit(new Values<Object>(topoTask));
            }

        } finally {
            synchronized (crawler) {
                crawler.getCrawlDataStructure().removeTask((TopoTask) obj);
            }
        }

        //logger.debug("Done Task");

    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);

    }

    @Override
    public Fields getSchema() {
        return new Fields("crawlServiceResponse");
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("crawlServiceResponse"));
    }
}
