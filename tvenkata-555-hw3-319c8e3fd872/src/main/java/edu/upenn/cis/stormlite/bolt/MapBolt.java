package edu.upenn.cis.stormlite.bolt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.upenn.cis455.mapreduce.CollectorWrapper;
import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStatsEnum;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.distributed.ConsensusTracker;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.mapreduce.Job;

/**
 * A simple adapter that takes a MapReduce "Job" and calls the "map"
 * on a per-tuple basis.
 * 
 * 
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MapBolt implements IRichBolt {
	static Logger log = LogManager.getLogger(MapBolt.class);

	Job mapJob;
	
	/**
	 * This object can help determine when we have
	 * reached enough votes for EOS
	 */
	ConsensusTracker votesForEos;

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordCounter, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();
    
	Fields schema = new Fields("key", "value");

	boolean sentEos = false;
	
	/**
     * This is where we send our output stream
     */
    private OutputCollector collector;
    
    private TopologyContext context;

    private WorkerServer workerServer;

	int currentVoteCount = 0;
	int neededVotesToComplete = 0;

	CollectorWrapper collectorWrapper;

	Set<String> votedEos = new HashSet<>();
    
    public MapBolt() {
    	workerServer = WorkerServer.currentMachineWorker;
    }
    
	/**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String,String> stormConf, 
    		TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.context = context;
        
        if (!stormConf.containsKey("mapClass"))
        	throw new RuntimeException("Mapper class is not specified as a config option");
        else {
        	String mapperClass = stormConf.get("mapClass");
        	
        	try {
				mapJob = (Job)Class.forName(mapperClass).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Unable to instantiate the class " + mapperClass);
			}
        }
        
        if (!stormConf.containsKey("spoutExecutors")) {
        	throw new RuntimeException("Mapper class doesn't know how many input spout executors");
        }
        
        // TODO: determine how many end-of-stream requests are needed, create a ConsensusTracker
        // or whatever else you need to determine when votes reach consensus

		// if consensus :

		int n = Integer.valueOf(stormConf.get("workerCount"));
		int k = Integer.valueOf(stormConf.get("spoutExecutors"));
		int l = Integer.valueOf(stormConf.get("mapExecutors"));

		//int eosVoteCount = (n - 1) * k * l + k;

		int eosVoteCount = n * k;

		neededVotesToComplete = eosVoteCount;
		votesForEos = new ConsensusTracker(neededVotesToComplete);
		collectorWrapper = new CollectorWrapper(collector, workerServer);

		synchronized (workerServer) {
			workerServer.getCurrentWorkerStats().startingVote(1, CurrentWorkerStatsEnum.MAP_PHASE);
		}


    }

    /**
     * Process a tuple received from the stream, incrementing our
     * counter and outputting a result
     */
    @Override
    public synchronized boolean execute(Tuple input) {
    	if (!input.isEndOfStream()) {
    		try {
				String key = input.getStringByField("key");
				String value = input.getStringByField("value");
				log.info(getExecutorId() + " received " + key + " / " + value + " from executor " + input.getSourceExecutor());

				if (sentEos) {
					throw new RuntimeException("We received data from " + input.getSourceExecutor() + " after we thought the stream had ended!");
				}
				synchronized (workerServer) {
					workerServer.getCurrentWorkerStats().incMapRead();
				}
				mapJob.map(key, value, collectorWrapper, executorId);
				context.incMapOutputs(key);
			} catch (Exception e) {
    			e.printStackTrace();
			}

    	} else if (input.isEndOfStream()) {
    		try {
				if (votedEos.contains(input.getSourceExecutor())) {
					return true;
				}
				votedEos.add(input.getSourceExecutor());
				log.info("Processing EOS from " + input.getSourceExecutor());
				log.info("VoteCount " + currentVoteCount + " : " + neededVotesToComplete + " " + executorId);
				if (votesForEos.voteForEos(input.getSourceExecutor())) {
					try {
						sentEos = true;
						synchronized (workerServer) {
							workerServer.getCurrentWorkerStats().endingVote(1, CurrentWorkerStatsEnum.MAP_PHASE);
						}
					} finally {
						collector.emitEndOfStream(executorId);
					}
				}
			} catch (Exception e) {
    			e.printStackTrace();
			}
    	}
    	return true;
    }

    /**
     * Shutdown, just frees memory
     */
    @Override
    public void cleanup() {
    }

    /**
     * Lets the downstream operators know our schema
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

    /**
     * Used for debug purposes, shows our exeuctor/operator's unique ID
     */
	@Override
	public String getExecutorId() {
		return executorId;
	}

	/**
	 * Called during topology setup, sets the router to the next
	 * bolt
	 */
	@Override
	public void setRouter(StreamRouter router) {
		this.collector.setRouter(router);
	}

	/**
	 * The fields (schema) of our output stream
	 */
	@Override
	public Fields getSchema() {
		return schema;
	}
}
