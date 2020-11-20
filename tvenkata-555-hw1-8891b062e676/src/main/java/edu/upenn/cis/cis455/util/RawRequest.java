package edu.upenn.cis.cis455.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Raw request structure.
 */

public class RawRequest {
    private String header;
    private String body;

    public String getBody() {
        return body;
    }

    public RawRequest(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public InputStream getHeaderInputStream() {
        InputStream is = new ByteArrayInputStream(header.getBytes());
        return is;
    }

}
