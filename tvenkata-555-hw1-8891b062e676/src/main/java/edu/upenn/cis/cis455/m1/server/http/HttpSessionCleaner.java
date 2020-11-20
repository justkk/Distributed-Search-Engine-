package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.m1.server.HttpServer;

public class HttpSessionCleaner implements Runnable {

    private HttpServer httpServer;

    public HttpSessionCleaner(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public void run() {

        while (true) {
            if(!httpServer.getServerStatus().isActive()) {
                return;
            }
            else {
                try{
                    httpServer.sessionCleaner();
                    Thread.sleep(Constants.getInstance().getSESSION_CLEANER_SLEEP_TIME() * 1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
