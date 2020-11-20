package edu.upenn.cis455.mapreduce.manager;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class TupleStore {

    @PrimaryKey(sequence = "TupleStoreIndex")
    private Integer pk;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String key;
    private String value;

    public TupleStore() {
    }

    public TupleStore(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getPk() {
        return pk;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
