package edu.upenn.cis.stormlite.bolt;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.distributed.ConsensusTracker;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.mapreduce.CollectorWrapper;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.Utils;
import edu.upenn.cis455.mapreduce.manager.BerkDbManager;
import edu.upenn.cis455.mapreduce.manager.DummyIterator;
import edu.upenn.cis455.mapreduce.manager.TupleStore;
import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStatsEnum;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * A simple adapter that takes a MapReduce "Job" and calls the "reduce"
 * on a per-tuple basis
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ReduceBolt implements IRichBolt {
    static Logger log = LogManager.getLogger(ReduceBolt.class);


    Job reduceJob;

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

    String subFolder = "temp";

    Fields schema = new Fields("key", "value");

    boolean sentEos = false;

    /**
     * Buffer for state, by key
     */
    Map<String, List<String>> stateByKey = new HashMap<>();

    /**
     * This is where we send our output stream
     */
    private OutputCollector collector;

    private TopologyContext context;

    int neededVotesToComplete = 0;

    int currentVoteCount = 0;

    private WorkerServer workerServer;

    private String databaseName;

    private BerkDbManager berkDbManager;

    boolean uniqueFlag = true;

    private Environment databaseEnvironment;
    CollectorWrapper collectorWrapper;

    Set<String> votedEos = new HashSet<>();

    public ReduceBolt() {
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String, String> stormConf,
                        TopologyContext context, OutputCollector collector) {

        workerServer = WorkerServer.currentMachineWorker;
        databaseName = workerServer.name + "_" + "Executor_" + executorId + "_" + stormConf.get("job");
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);

        subFolder = Paths.get(workerServer.getConfig().get("databaseLocation"), databaseName).toAbsolutePath().toString();
        Utils.createDirectory(subFolder);
        try {
            Utils.checkDirectory(subFolder);
        } catch (Exception e) {
            subFolder = workerServer.getConfig().get("databaseLocation");
        }
        databaseEnvironment = new Environment(new File(subFolder), environmentConfig);
        berkDbManager = new BerkDbManager(databaseEnvironment, databaseName);


        this.collector = collector;
        this.context = context;

        if (!stormConf.containsKey("reduceClass"))
            throw new RuntimeException("Mapper class is not specified as a config option");
        else {
            String mapperClass = stormConf.get("reduceClass");

            try {
                reduceJob = (Job) Class.forName(mapperClass).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException("Unable to instantiate the class " + mapperClass);
            }
        }
        if (!stormConf.containsKey("mapExecutors") || !stormConf.containsKey("workerCount")) {
            throw new RuntimeException("Reducer class doesn't know how many map bolt executors");
        }

        // TODO: determine how many EOS votes needed and set up ConsensusTracker (or however
        // you want to handle consensus)

        int workerCount = Integer.valueOf(stormConf.get("workerCount"));
        int eachMachineExec = Integer.valueOf(stormConf.get("mapExecutors"));

        int n = Integer.valueOf(stormConf.get("workerCount"));
        int k = Integer.valueOf(stormConf.get("mapExecutors"));
        int l = Integer.valueOf(stormConf.get("reduceExecutors"));

        //int eosVoteCount = (n - 1) * k * l + k;

        int eosVoteCount = n * k;
        votesForEos = new ConsensusTracker(eosVoteCount);
        neededVotesToComplete = eosVoteCount;
        collectorWrapper = new CollectorWrapper(collector, workerServer);
    }

    /**
     * Process a tuple received from the stream, buffering by key
     * until we hit end of stream
     */
    @Override
    public synchronized boolean execute(Tuple input) {
        if (sentEos) {
            if (!input.isEndOfStream())
                throw new RuntimeException("We received data after we thought the stream had ended!");
            // Already done!
            return false;
        } else if (input.isEndOfStream()) {

            if (votedEos.contains(input.getSourceExecutor())) {
                return true;
            }
            votedEos.add(input.getSourceExecutor());
            log.info("Processing EOS from " + input.getSourceExecutor());
            if (votesForEos.voteForEos(input.getSourceExecutor())) {
                try {
                    sentEos = true;
                    synchronized (workerServer) {
                        if (uniqueFlag) {
                            workerServer.getCurrentWorkerStats().startingVote(1,
                                    CurrentWorkerStatsEnum.REDUCE_PHASE);
                            uniqueFlag = false;
                        }
                    }
                    EntityCursor<String> entityCursor = berkDbManager.getKeyCursor();
                    try {
                        String key = entityCursor.nextNoDup();
                        while (key != null) {

                            System.out.println("Reducing the Key " + key);

                            context.incReduceOutputs(key);
//                        synchronized (workerServer) {
//                            workerServer.getCurrentWorkerStats().incReduceRead();
//                        }

                            EntityCursor<TupleStore> tupleStoreEntityCursor = berkDbManager.getCursorForKey(key);
                            try {
                                Iterator<TupleStore> tupleStoreIterator = tupleStoreEntityCursor.iterator();
                                DummyIterator dummyIterator = new DummyIterator(tupleStoreIterator);
                                reduceJob.reduce(key, dummyIterator, collectorWrapper, executorId);
                            } finally {
                                tupleStoreEntityCursor.close();
                            }
                            key = entityCursor.nextNoDup();
                        }

                    } finally {
                        entityCursor.close();
                    }

                    berkDbManager.deleteStore();
                    Utils.deleteDirectory(subFolder);

                    synchronized (workerServer) {
                        workerServer.getCurrentWorkerStats().endingVote(1,
                                CurrentWorkerStatsEnum.REDUCE_PHASE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    collector.emitEndOfStream(executorId);
                }
            }

        } else {
            try {
                log.info("Processing " + input.toString() + " from " + input.getSourceExecutor());
                String key = input.getStringByField("key");
                String value = input.getStringByField("value");
                berkDbManager.addTuple(key, value);
                synchronized (workerServer) {
                    workerServer.getCurrentWorkerStats().incReduceRead();
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
        berkDbManager.close();
        if(subFolder!=null) {
            Utils.deleteDirectory(subFolder);
        }
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
