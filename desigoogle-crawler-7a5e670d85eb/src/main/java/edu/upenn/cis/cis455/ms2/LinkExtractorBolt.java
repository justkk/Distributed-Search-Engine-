package edu.upenn.cis.cis455.ms2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.ms2.model.TopoTask;

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
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

        Object obj = input.getValueByField("crawlServiceResponse");

        CrawlServiceResponse crawlServiceResponse = (CrawlServiceResponse) ((TopoTask) obj).getObject();

        try {
            CrawlServiceResponse processedResponse = null;
            if (crawlServiceResponse.isProcessed()) {
                processedResponse = crawlServiceResponse;
            } else {
                processedResponse = crawlerService.processDocument(new ReadyQueueInstance(crawlServiceResponse.getProcessContext().getUrlInfo(),
                                String.valueOf(crawlServiceResponse.getProcessContext().getParentId())),
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
                        if (crawler.getCrawlDataStructure().addOnlyNotPresentReadyQueueInstances(readyQueueInstanceTemp)) {
                            count += 1;
                        }
                    }
                    for (WaitingQueueInstance waitingQueueInstance : newWaitsToBeInserted) {
                        crawler.getCrawlDataStructure().removeFromReadyQueueInstanceSet(waitingQueueInstance
                                .getReadyQueueInstance());
                        crawler.getCrawlDataStructure().addWaitQueueInstance(waitingQueueInstance);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            synchronized (crawler) {
                crawler.getCrawlDataStructure().removeTask((TopoTask) obj);
            }
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    	this.crawler = Crawler.getCrawler();
        this.crawlerService = crawler.getCrawlerService();
    }

    public Fields getSchema() {
        return schema;
    }

    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
