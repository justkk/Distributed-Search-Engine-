package edu.upenn.cis.stormlite;

import java.util.Map;
import java.util.UUID;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A trivial bolt that simply outputs its input stream to the
 * console
 * 
 * @author zives
 *
 */
public class PrintBolt implements IRichBolt {

	static Logger log = LogManager.getLogger(PrintBolt.class);
	
	Fields myFields = new Fields();

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the PrintBolt, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

	@Override
	public void cleanup() {
		// Do nothing

	}

	@Override
	public void execute(Tuple input) {

		System.out.println(getExecutorId() + ": " + input.toString());
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// Do nothing
	}

	@Override
	public String getExecutorId() {
		return executorId;
	}

	@Override
	public void setRouter(IStreamRouter router) {
		// Do nothing
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(myFields);
	}

	@Override
	public Fields getSchema() {
		return myFields;
	}

}
