package edu.upenn.cis.cis455.xpathengine;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class XMLEventsSpout implements IRichSpout {

	static Logger log = LogManager.getLogger(XMLEventsSpout.class);

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordSpout, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    /**
	 * The collector is the destination for tuples; you "emit" tuples there
	 */
	SpoutOutputCollector collector;

	/**
	 * This is a simple file reader for words.txt
	 */
    BufferedReader reader;
	Random r = new Random();

	String[] words = {"big", "bad", "wolf", "little", "red", "riding", "hood"};

    public XMLEventsSpout() {
    	log.debug("Starting xml spout");
    }

    /**
     * Initializes the instance of the spout (note that there can be multiple
     * objects instantiated)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        
        try {
        	log.debug(getExecutorId() + " opening file reader");
			reader = new BufferedReader(new FileReader(XMLEventsSpout.class.getClassLoader().getResource("words.txt").getPath()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Shut down the spout
     */
    @Override
    public void close() {
    	if (reader != null)
	    	try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    /**
     * The real work happens here, in incremental fashion.  We process and output
     * the next item(s).  They get fed to the collector, which routes them
     * to targets
     */
    @Override
    public void nextTuple() {
    	if (reader != null) {
	    	try {
		    	String line = reader.readLine();
		    	if (line != null) {
		        	log.debug(getExecutorId() + " read from file " + line);
		    		String[] words = line.split("[ \\t\\,.]");
		
		    		for (String word: words) {
		            	log.debug(getExecutorId() + " emitting " + word);
		    	        this.collector.emit(new Values(word));
		    		}
		    	} else {
		    		int pos = r.nextInt(words.length);
		    		String word = words[pos];
	            	log.debug(getExecutorId() + " emitting " + word);
	    	        this.collector.emit(new Values(word));
		    	}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
    	}
        Thread.yield();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }


	public String getExecutorId() {
		
		return executorId;
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
