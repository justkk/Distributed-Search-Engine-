package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;

import java.util.*;

public class HttpSession extends Session {

    private String id;
    private Date creationDate;
    private Date lastAccessedDate;
    private boolean valid;
    private int maxInactiveInterval;

    private Map<String, Object> attibuteMap;

    private boolean newSession;

    private static long sessionCounter = 0;
    private static String sessionPrefix = UUID.randomUUID().toString();

    public static synchronized HttpSession createHttpSession(boolean newSession) {
        sessionCounter += 1;
        return new HttpSession(sessionPrefix + sessionCounter, newSession);
    }

    public HttpSession(String id, boolean newSession) {
        this.id = id;
        this.creationDate = new Date();
        this.lastAccessedDate = new Date();
        this.valid = true;
        this.newSession = newSession;
        this.attibuteMap = new HashMap<>();
        this.maxInactiveInterval = Constants.getInstance().getSESSION_AGE();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long creationTime() {
        return creationDate.getTime();
    }

    @Override
    public long lastAccessedTime() {
        return lastAccessedDate.getTime();
    }

    @Override
    public synchronized void invalidate() {
        valid = false;
        maxInactiveInterval = -1;
    }

    @Override
    public int maxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public synchronized void maxInactiveInterval(int interval) {
        maxInactiveInterval = interval;
    }

    @Override
    public synchronized void access() {
        lastAccessedDate = new Date();
    }

    @Override
    public synchronized void attribute(String name, Object value) {
        attibuteMap.put(name, value);
    }

    @Override
    public synchronized Object attribute(String name) {
        if(attibuteMap.containsKey(name)) {
            return attibuteMap.get(name);
        }
        return null;
    }

    @Override
    public Set<String> attributes() {
        return attibuteMap.keySet();
    }

    @Override
    public void removeAttribute(String name) {
        attibuteMap.remove(name);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
