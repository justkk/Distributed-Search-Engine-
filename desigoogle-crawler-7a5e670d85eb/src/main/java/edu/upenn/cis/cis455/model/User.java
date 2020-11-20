package edu.upenn.cis.cis455.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.io.Serializable;

@Entity
public class User implements Serializable {

    @PrimaryKey(sequence = "userId")
    Integer userId;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    String userName;
    String password;
    String firstName;
    String lastName;

    public User() {
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
