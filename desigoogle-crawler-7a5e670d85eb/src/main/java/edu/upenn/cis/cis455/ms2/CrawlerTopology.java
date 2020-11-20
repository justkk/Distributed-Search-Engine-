package edu.upenn.cis.cis455.ms2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis.cis455.crawler.Crawler;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class CrawlerTopology {

    //private Crawler crawler;

    public CrawlerTopology(Crawler crawler) {
        //this.crawler = crawler;
    }

    private LocalCluster cluster;

    private static final String CRAWLER_SPOUT = "CRAWLER_SPOUT";
    private static final String DOCUMENT_FETCHER_BOLT = "DOCUMENT_FETCHER_BOLT";
    private static final String LINK_EXTRACTOR_BOLT = "LINK_EXTRACTOR_BOLT";
    private static final String DOM_PARSER_BOLT = "DOM_PARSER_BOLT";
    private static final String PATH_MATCHER_BOLT = "PATH_MATCHER_BOLT";


    private String CRAWLER_NAME = "crawler";


    public void buildTopologyAndStart() {

        Config config = new Config();

        config.setNumWorkers(3);

        CrawlerQueueSpout crawlerQueueSpout = new CrawlerQueueSpout();
        DocumentFetcherBolt documentFetcherBolt = new DocumentFetcherBolt();
        LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();
        //DomParserBolt domParserBolt = new DomParserBolt(crawler);
        //PathMatcherBolt pathMatcherBolt = new PathMatcherBolt(crawler);

        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the words
        builder.setSpout(CRAWLER_SPOUT, crawlerQueueSpout, 1);
        builder.setBolt(DOCUMENT_FETCHER_BOLT, documentFetcherBolt, 5).fieldsGrouping(CRAWLER_SPOUT, new Fields("hostHash"));
        builder.setBolt(LINK_EXTRACTOR_BOLT, linkExtractorBolt, 5).shuffleGrouping(DOCUMENT_FETCHER_BOLT);
        //builder.setBolt(DOM_PARSER_BOLT, domParserBolt, 1).shuffleGrouping(DOCUMENT_FETCHER_BOLT);
        //builder.setBolt(PATH_MATCHER_BOLT, pathMatcherBolt, 1).fieldsGrouping(DOM_PARSER_BOLT,
        //        new Fields("docId"));


        //cluster = new LocalCluster();

        StormTopology topo = builder.createTopology();

        /*ObjectMapper mapper = new ObjectMapper();
        try {
            String str = mapper.writeValueAsString(topo);

            System.out.println("The StormLite topology is:\n" + str);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        try {
            StormSubmitter.submitTopology(CRAWLER_NAME, config,
                    builder.createTopology());
        } catch (AlreadyAliveException e) {
            e.printStackTrace();
        } catch (InvalidTopologyException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }

        //cluster.submitTopology(CRAWLER_NAME, config,
        //        builder.createTopology());
    }


//    public void shutdown() {
//        synchronized (crawler) {
//            crawler.getCrawlDataStructure().getRunningTasks().clear();
//        }
//        if (cluster != null) {
//            cluster.killTopology(CRAWLER_NAME);
//            cluster.shutdown();
//            try {
////                Field f = LocalCluster.class.getDeclaredField("executor");
////                f.setAccessible(true);
////                ExecutorService executorService = (ExecutorService) f.get(cluster);
////                executorService.shutdown();
//            } catch (Exception e) {
//
//            }
//        }
//    }
}
