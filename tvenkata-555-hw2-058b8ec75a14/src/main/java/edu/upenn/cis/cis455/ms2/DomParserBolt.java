package edu.upenn.cis.cis455.ms2;

import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.DocType;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.ms2.model.TopoTask;
import edu.upenn.cis.cis455.utils.HTMLTraversorHandler;
import edu.upenn.cis.cis455.xpathengine.XMLTraversorHandler;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.UUID;

public class DomParserBolt implements IRichBolt {

    private String executorId = UUID.randomUUID().toString();
    private Fields schema = new Fields("docId", "occuranceEvent");

    private Crawler crawler;
    private OutputCollector collector;

    public DomParserBolt(Crawler crawler) {
        this.crawler = crawler;
    }

    public DomParserBolt() {
        crawler = Crawler.getCrawler();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

        Object obj = input.getObjectByField("crawlServiceResponse");

        try {
            CrawlServiceResponse crawlServiceResponse = (CrawlServiceResponse) ((TopoTask) obj).getObject();
            ProcessContext processContext = crawlServiceResponse.getProcessContext();

            if (!crawlServiceResponse.isSuccess() || processContext.getUrlDataInfo() == null ) {
                return;
            }

            URLDataInfo urlDataInfo = processContext.getUrlDataInfo();
            String docId = String.valueOf(urlDataInfo.getDocOnlyInfo().getId());
            String data = urlDataInfo.getDocOnlyInfo().getContent();
            DocType docType = DocType.getTypeFromContentType(processContext.getGetInfo().getContentType());
//            if (docType != DocType.XML) {
//                return;
//            }

            try {
                if(docType == DocType.HTML || docType == DocType.XML) {
                    OccurrenceEvent occurrenceEvent = new OccurrenceEvent(null);
                    occurrenceEvent.setNewDocument(true);
                    emitEvent(docId, occurrenceEvent);
                }
                if(docType == DocType.XML) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    XMLTraversorHandler handler = new XMLTraversorHandler();
                    handler.setDocId(docId);
                    handler.setDomParserBolt(this);
                    saxParser.parse(new InputSource(new StringReader(data)), handler);
                } else if(docType == DocType.HTML) {
                    HTMLTraversorHandler htmlTraversorHandler = new HTMLTraversorHandler(data,
                            processContext.getUrlInfo().getUrl().toString(), this, docId);
                    htmlTraversorHandler.setDocId(docId);
                    htmlTraversorHandler.setDomParserBolt(this);
                    htmlTraversorHandler.parseHTML();
                }

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } finally {
                OccurrenceEvent occurrenceEvent = new OccurrenceEvent(null);
                occurrenceEvent.setCleanup(true);
                emitEvent(docId, occurrenceEvent);
            }

        } finally {
            synchronized (crawler) {
                crawler.getCrawlDataStructure().removeTask((TopoTask) obj);
            }
        }
    }

    public void emitEvent(String docId, OccurrenceEvent occurrenceEvent) {
        TopoTask topoTask = new TopoTask(occurrenceEvent);
        synchronized (crawler) {
            crawler.getCrawlDataStructure().addTask(topoTask, 1);
        }
        collector.emit(new Values<Object>(docId, topoTask));

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
