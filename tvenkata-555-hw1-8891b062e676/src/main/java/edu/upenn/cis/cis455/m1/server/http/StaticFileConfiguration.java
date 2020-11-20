package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.m1.server.implementations.FileRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

/***
 *
 * util for static file information.
 */
public class StaticFileConfiguration {

    private String staticFolderLocation;
    private FileRequestHandler fileRequestHandler;

    public void setStaticFolderLocation(String staticFolderLocation) {
        this.staticFolderLocation = staticFolderLocation;
        this.fileRequestHandler.setStaticFolderLocation(staticFolderLocation);
    }

    public StaticFileConfiguration(String staticFolderLocation) {
        this.staticFolderLocation = staticFolderLocation;
        this.fileRequestHandler = new FileRequestHandler(staticFolderLocation);
    }


    public void fetchFile(Request request, Response response) {
        fileRequestHandler.handleFetch(request, response);
    }

    public void lookFile(Request request, Response response) {
        fileRequestHandler.handleLookUp(request, response);
    }

}
