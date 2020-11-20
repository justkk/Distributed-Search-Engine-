package edu.upenn.cis.cis455.storage.berkDb;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataBaseConnectorConfig {


    private Map<String, Database> databaseConfigMap = new HashMap<>();

    private Environment databaseEnvironment;

    public DataBaseConnectorConfig() {
    }

    public DataBaseConnectorConfig(String location) {

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        databaseEnvironment = new Environment(new File(location), environmentConfig);


//        databaseConfigMap.put(Constants.getInstance().getURL_META_DATABASE(),
//                getDatabase(Constants.getInstance().getURL_META_DATABASE()));
//        databaseConfigMap.put(Constants.getInstance().getURL_DOC_DATABASE(),
//                getDatabase(Constants.getInstance().getURL_DOC_DATABASE()));
//        databaseConfigMap.put(Constants.getInstance().getUSER_RECORD_DATABASE(),
//                getDatabase(Constants.getInstance().getUSER_RECORD_DATABASE()));
//
//        databaseConfigMap.put(Constants.getInstance().getHOST_ROBOT_DATABASE(),
//                getDatabase(Constants.getInstance().getHOST_ROBOT_DATABASE()));

    }

    public Database getDatabaseInstance(String name) {

        if (databaseConfigMap.containsKey(name)) {
            return databaseConfigMap.get(name);
        }
        return null;
    }


    private Database getDatabase(String databaseName) {

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        Database currentDatabase = databaseEnvironment.openDatabase(null, databaseName, dbConfig);
        return currentDatabase;
    }

    @Override
    public void finalize() {

        for (Database database : databaseConfigMap.values()) {
            database.close();
        }
        databaseEnvironment.close();
    }

    public Environment getDatabaseEnvironment() {
        return databaseEnvironment;
    }

    public void setDatabaseEnvironment(Environment databaseEnvironment) {
        this.databaseEnvironment = databaseEnvironment;
    }
}
