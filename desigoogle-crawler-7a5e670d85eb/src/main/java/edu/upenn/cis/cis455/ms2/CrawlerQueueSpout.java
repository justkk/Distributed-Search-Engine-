package edu.upenn.cis.cis455.ms2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.ms2.model.TopoTask;

public class CrawlerQueueSpout implements IRichSpout {

    static Logger log = LogManager.getLogger(CrawlerQueueSpout.class);

    private String executorId = UUID.randomUUID().toString();

    private SpoutOutputCollector collector;


    private Crawler crawler;

    public CrawlerQueueSpout(Crawler crawler) {
        this.crawler = crawler;
    }

    public CrawlerQueueSpout() {
        
    }

    @Override
    public void open(Map config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
        this.crawler = Crawler.getCrawler();
        this.crawler.initializeCleaner();
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

                    readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                    while (readyQueueInstance != null &&
                            !crawler.getCrawlDataStructure().addOnlyNotPresentReadyQueueInstanceSet(readyQueueInstance)) {
                        System.out.println("Already Seen This Task, checking other one " +  readyQueueInstance.getUrlInfo().getUrl().toString());
                        readyQueueInstance = crawler.getCrawlDataStructure().getReadyQueueInstance();
                    }

                    if (readyQueueInstance == null) {

                        Thread.sleep(1000);

                    } else {
                        System.out.println("Processing " +  readyQueueInstance.getUrlInfo().getUrl().toString());

//                        crawler.getCrawlDataStructure().addToReadyQueueInstanceSet(readyQueueInstance);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(readyQueueInstance!=null) {
                    TopoTask topoTask = new TopoTask(readyQueueInstance);
                    crawler.getCrawlDataStructure().addTask(topoTask, 1);
                    this.collector.emit(new Values(topoTask, getHostHash(readyQueueInstance)));
                }
            }
            Thread.yield();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // nothing as of now.
        }



        //}
    }

    public String getHostHash(ReadyQueueInstance readyQueueInstance) {
        String url = readyQueueInstance.getUrlInfo().getUrl().getProtocol() + "://"
                + readyQueueInstance.getUrlInfo().getHostName() + ":"
                + String.valueOf(readyQueueInstance.getUrlInfo().getPortNo());

        return url;
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("readyQueueInstance", "hostHash"));
    }

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ack(Object msgId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fail(Object msgId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
