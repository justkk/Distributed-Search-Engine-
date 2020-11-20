package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.models.RouteHandlerMapEntry;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestRouteMatcher {

    /*
    Param Testing.
     */
    @Test
    public void testParamProcessing() throws IOException {
        RouteMapEntry routeMapEntry = new RouteHandlerMapEntry("/hello/:name", RequestTypeEnum.GET,
                null);
        assertTrue(routeMapEntry.matchPath("/hello/temp"));
        assertFalse(routeMapEntry.matchPath("/hello/temp/"));
        assertFalse(routeMapEntry.matchPath("/hello/temp/temp"));
    }

    @Test
    public void testWildCard() {
        RouteMapEntry routeMapEntry = new RouteHandlerMapEntry("/hello/:name/*", RequestTypeEnum.GET,
                null);
        assertFalse(routeMapEntry.matchPath("/hello/temp"));
        assertTrue(routeMapEntry.matchPath("/hello/temp/"));
        assertTrue(routeMapEntry.matchPath("/hello/temp/temp"));
    }

    @Test
    public void testComplexMatching() {

        RouteMapEntry routeMapEntry = new RouteHandlerMapEntry("/x/*/y/:param2/*", RequestTypeEnum.GET,
                null);
        assertFalse(routeMapEntry.matchPath("/x/y/nikhil/"));
        assertTrue(routeMapEntry.matchPath("/x/temp/y/value/"));
        assertTrue(routeMapEntry.matchPath("/x/temp/y/value/temp"));

    }



}
