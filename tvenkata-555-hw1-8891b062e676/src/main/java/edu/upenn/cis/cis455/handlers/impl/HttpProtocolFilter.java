package edu.upenn.cis.cis455.handlers.impl;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;

/*
 ***
 * HttpProtocolFilter is a implementation of Filter.
 * It checks headers specific to a protocol.
 */


public class HttpProtocolFilter implements Filter {

    static final Logger logger = LogManager.getLogger(HttpProtocolFilter.class);

    @Override
    public void handle(Request request, Response response) throws Exception {

        logger.info("Checking protocol and its required headers");

        if (request.protocol() == null) {
            logger.error("protocol is null");
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST);
        }

        if (request.protocol().equals("HTTP/1.1") && request.host() == null) {
            logger.error("HTTP/1.1 got empty host");
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST);
        }

    }
}
