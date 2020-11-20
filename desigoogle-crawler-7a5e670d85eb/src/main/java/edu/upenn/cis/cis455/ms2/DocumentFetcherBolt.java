package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.ms2.model.TopoTask;

import java.util.Map;
import java.util.UUID;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

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
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

        Object obj = input.getValueByField("readyQueueInstance");
        ReadyQueueInstance readyQueueInstance = (ReadyQueueInstance) ((TopoTask) obj).getObject();
        if (readyQueueInstance == null) {
            return;
        }
        try {
            CrawlServiceResponse crawlServiceResponse = null;
            try {
                crawlServiceResponse = crawlerService.processRequest(readyQueueInstance);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if(crawlServiceResponse != null) {
                TopoTask topoTask = new TopoTask(crawlServiceResponse);
                synchronized (crawler) {
                    crawler.getCrawlDataStructure().addTask(topoTask, 1);
                }
                collector.emit(new Values(topoTask));
            }

        } catch (Exception e) {
            e.printStackTrace();;
        }
        finally {
            synchronized (crawler) {
                crawler.getCrawlDataStructure().removeTask((TopoTask) obj);
            }
        }

        //logger.debug("Done Task");

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.crawler = Crawler.getCrawler();
        this.crawlerService = crawler.getCrawlerService();
    }

    public Fields getSchema() {
        return new Fields("crawlServiceResponse");
    }

    public String getExecutorId() {
        return executorId;
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("crawlServiceResponse"));
    }

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
