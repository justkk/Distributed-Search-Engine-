package edu.upenn.cis.cis455.m1.server.http.models;

import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerConfig;

/***
 * HttpServer config extends the basic server config.
 * It has extra field of staticFolderLocation
 */

public class HttpServerConfig extends ServerConfig {

    private String staticFolderLocation = "./www";

    public HttpServerConfig() {
    }

    public String getStaticFolderLocation() {
        return staticFolderLocation;
    }

    public void setStaticFolderLocation(String staticFolderLocation) {
        this.staticFolderLocation = staticFolderLocation;
    }
}
