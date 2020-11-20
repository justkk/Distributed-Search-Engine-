package edu.upenn.cis.cis455.playgroud;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.*;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

@Entity
public class Model2 {
    @PrimaryKey
    private String id2;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,  relatedEntity = Model1.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String nameId;

    private String lastName;

    public Model2(String id, String nameId, String lastName) {
        this.id2 = id;
        this.nameId = nameId;
        this.lastName = lastName;
    }

    public Model2() {
    }

    public static void main(String[] args) {

        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/Penn/CourseWork/IWS/projects/555-hw2/dbs");
        StoreConfig stConf;
        EntityStore store1;
        EntityStore store2;

        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        //stConf.set

        store1 = new EntityStore(dataBaseConnectorConfig.getDatabaseEnvironment(),
                "model1", stConf);

        store2 = new EntityStore(dataBaseConnectorConfig.getDatabaseEnvironment(),
                "model1", stConf);



        PrimaryIndex<String, Model1> primaryIndex1 = store1.getPrimaryIndex(String.class, Model1.class);
        PrimaryIndex<String, Model2> primaryIndex2 = store2.getPrimaryIndex(String.class, Model2.class);
        SecondaryIndex<String, String, Model1> secondaryIndex1 = store1.getSecondaryIndex(primaryIndex1, String.class, "name");
        SecondaryIndex<String, String, Model2> secondaryIndex2 = store2.getSecondaryIndex(primaryIndex2, String.class, "nameId");
        Transaction txn = dataBaseConnectorConfig.getDatabaseEnvironment().beginTransaction(null, null);

        Model1 model1 = new Model1("1", "nikhil");
        primaryIndex1.put(txn, model1);
        Model2 model2 = new Model2("1", null, "thdopupunuri");
        primaryIndex2.put(txn, model2);
        Model2 model22 = new Model2("2", "1", "tho");
        primaryIndex2.put(txn, model22);

        txn.commit();;


    }
}
