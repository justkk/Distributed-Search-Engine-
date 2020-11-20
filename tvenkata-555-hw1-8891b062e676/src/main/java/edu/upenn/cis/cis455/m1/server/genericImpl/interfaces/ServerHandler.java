package edu.upenn.cis.cis455.m1.server.genericImpl.interfaces;

import java.net.Socket;

public interface ServerHandler {

    Runnable modelRequest(Socket socket);

}
