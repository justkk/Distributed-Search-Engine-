package edu.upenn.cis.cis455.m1.server.genericImpl.models;

public class ServerStatus {

    private volatile boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void markInActive() {
        isActive = false;
    }

    public void markActive() {
        isActive = true;
    }

    public ServerStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public ServerStatus() {
        this.isActive = true;
    }
}
