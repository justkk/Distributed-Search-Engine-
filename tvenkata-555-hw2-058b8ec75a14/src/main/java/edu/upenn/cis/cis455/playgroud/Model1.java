package edu.upenn.cis.cis455.playgroud;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Model1 {

    @PrimaryKey
    private String id;

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String name;

    public Model1(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Model1() {
    }
}
