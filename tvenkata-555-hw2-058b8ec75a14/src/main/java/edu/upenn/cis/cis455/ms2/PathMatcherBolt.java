package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.model.UserChannelInfo;
import edu.upenn.cis.cis455.model.index.DocChannelIndex;
import edu.upenn.cis.cis455.model.index.DocChannelIndexKey;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineStateMachineImpl;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class PathMatcherBolt implements IRichBolt {

    private Crawler crawler;
    private String executorId = UUID.randomUUID().toString();
    private Fields schema = new Fields();
    private StorageInterfaceImpl storageInterface;
    List<UserChannelInfo> userChannelInfoList = new ArrayList<>();
    private List<String> channels = new ArrayList<>();
    Map<String, List<Integer>> docMatchingChannelsMap = new HashMap<>();

    Map<String, XPathEngineStateMachineImpl> xPathEngineStateMachineHashMap = new HashMap<>();

    public PathMatcherBolt(Crawler crawler) {
        this.crawler = crawler;
    }

    public PathMatcherBolt() {
        this.crawler = Crawler.getCrawler();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {



        Object obj = input.getObjectByField("occuranceEvent");
        String docId = input.getStringByField("docId");

       // System.out.println(docId);

        if(storageInterface ==null)
            return;

        try {
            OccurrenceEvent occurrenceEvent = (OccurrenceEvent) ((TopoTask) obj).getObject();
            if (occurrenceEvent.isNewDocument()) {
                XPathEngineStateMachineImpl xPathEngineStateMachine = (XPathEngineStateMachineImpl)
                        XPathEngineFactory.getXPathEngine();
                xPathEngineStateMachine.setXPaths(channels.toArray(new String[0]));
                storageInterface.getIndexManager().delete(Integer.valueOf(docId));
                xPathEngineStateMachineHashMap.put(docId, xPathEngineStateMachine);
                docMatchingChannelsMap.put(docId, new ArrayList<>());
                return;
            } else if (occurrenceEvent.isCleanup()) {
                xPathEngineStateMachineHashMap.remove(docId);
                docMatchingChannelsMap.remove(docId);
                return;
            }

            XPathEngineStateMachineImpl xPathEngineStateMachine = xPathEngineStateMachineHashMap.get(docId);
            List<Integer> matchingDocuments = docMatchingChannelsMap.get(docId);

            if(xPathEngineStateMachine == null || matchingDocuments == null) {
                return;
            }

            boolean[] matchingList = xPathEngineStateMachine.evaluateEvent(occurrenceEvent);
            if(matchingList == null) {
                return;
            }

            for(int i=0; i< matchingList.length; i++) {
                if(matchingList[i] && !matchingDocuments.contains(i)) {
                    UserChannelInfo userChannelInfo = userChannelInfoList.get(i);
                    DocChannelIndexKey docChannelIndexKey = new DocChannelIndexKey(Integer.valueOf(docId),
                            userChannelInfo.getChannelName());
                    DocChannelIndex docChannelIndex = new DocChannelIndex(docChannelIndexKey);
                    storageInterface.getIndexManager().insert(docChannelIndex);
                    matchingDocuments.add(i);
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
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        storageInterface = (StorageInterfaceImpl) crawler.getDb();
        userChannelInfoList = storageInterface.getUserChannelDataManager().getAllChannels();
        channels = userChannelInfoList.stream().map(userChannelInfo
                -> userChannelInfo.getxPath()).collect(Collectors.toList());

    }

    public void addThinginDatabase(String docId, String channelName) {

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
