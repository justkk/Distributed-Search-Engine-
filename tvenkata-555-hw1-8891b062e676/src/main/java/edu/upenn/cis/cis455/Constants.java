package edu.upenn.cis.cis455;

/**
 * Constants class for handling server default configuration.
 *
 * CHUNKED_ENCODING for enabling chunked encoding
 * CONNECTION_ALIVE_SUPPORT to enable connection live support.
 * IDLE_CONNECTION_TIME: for keep-alive
 * SOCKET_TIME_OUT to close the socket.
 * SESSION_CLEANER_SLEEP_TIME: interval when to clean the sessions.
 * MAX_REQUEST_COUNT_PER_SOCKET: max requests in a keep-alive tcp connection
 * THREAD_COUNT: Number of worker threads.
 */

public class Constants {

    private static Constants instance = null;
    private final boolean CONNECTION_ALIVE_SUPPORT = false;
    private  int SESSION_AGE = 20; // 30 sec // session Cookie Age.
    private  int CHUNK_SIZE = 100;
    private  boolean CHUNKED_ENCODING = false;
    private  int ERROR_LOG_SIZE = 10000;
    private  int COOKIE_AGE = 30;
    private  int IDLE_CONNECTION_TIME = 30; // sec
    private  int SOCKET_TIME_OUT = 10; // sec
    private  int SESSION_CLEANER_SLEEP_TIME = 1;
    private  int MAX_REQUEST_COUNT_PER_SOCKET = 100;
    private  int THREAD_COUNT = 50;


    public static Constants getInstance() {

        synchronized (Constants.class) {
            if (Constants.instance == null) {
                Constants.instance = new Constants();
            }
            return Constants.instance;
        }
    }


    private final int requestQueueSize = 100;
    private final String remoteIp = "nikhil@penn";

    public String getRemoteIp() {
        return remoteIp;
    }

    public int getRequestQueueSize() {
        return requestQueueSize;
    }

    public boolean isCONNECTION_ALIVE_SUPPORT() {
        return CONNECTION_ALIVE_SUPPORT;
    }

    public int getSESSION_AGE() {
        return SESSION_AGE;
    }

    public int getCHUNK_SIZE() {
        return CHUNK_SIZE;
    }

    public boolean isCHUNKED_ENCODING() {
        return CHUNKED_ENCODING;
    }

    public int getERROR_LOG_SIZE() {
        return ERROR_LOG_SIZE;
    }

    public int getCOOKIE_AGE() {
        return COOKIE_AGE;
    }

    public int getIDLE_CONNECTION_TIME() {
        return IDLE_CONNECTION_TIME;
    }

    public int getSOCKET_TIME_OUT() {
        return SOCKET_TIME_OUT;
    }

    public int getSESSION_CLEANER_SLEEP_TIME() {
        return SESSION_CLEANER_SLEEP_TIME;
    }

    public int getMAX_REQUEST_COUNT_PER_SOCKET() {
        return MAX_REQUEST_COUNT_PER_SOCKET;
    }

    public int getTHREAD_COUNT() {
        return THREAD_COUNT;
    }

    public void setSESSION_AGE(int SESSION_AGE) {
        this.SESSION_AGE = SESSION_AGE;
    }

    public void setCHUNK_SIZE(int CHUNK_SIZE) {
        this.CHUNK_SIZE = CHUNK_SIZE;
    }

    public void setCHUNKED_ENCODING(boolean CHUNKED_ENCODING) {
        this.CHUNKED_ENCODING = CHUNKED_ENCODING;
    }

    public void setERROR_LOG_SIZE(int ERROR_LOG_SIZE) {
        this.ERROR_LOG_SIZE = ERROR_LOG_SIZE;
    }

    public void setCOOKIE_AGE(int COOKIE_AGE) {
        this.COOKIE_AGE = COOKIE_AGE;
    }

    public void setIDLE_CONNECTION_TIME(int IDLE_CONNECTION_TIME) {
        this.IDLE_CONNECTION_TIME = IDLE_CONNECTION_TIME;
    }

    public void setSOCKET_TIME_OUT(int SOCKET_TIME_OUT) {
        this.SOCKET_TIME_OUT = SOCKET_TIME_OUT;
    }

    public void setSESSION_CLEANER_SLEEP_TIME(int SESSION_CLEANER_SLEEP_TIME) {
        this.SESSION_CLEANER_SLEEP_TIME = SESSION_CLEANER_SLEEP_TIME;
    }

    public void setMAX_REQUEST_COUNT_PER_SOCKET(int MAX_REQUEST_COUNT_PER_SOCKET) {
        this.MAX_REQUEST_COUNT_PER_SOCKET = MAX_REQUEST_COUNT_PER_SOCKET;
    }

    public void setTHREAD_COUNT(int THREAD_COUNT) {
        this.THREAD_COUNT = THREAD_COUNT;
    }
}
