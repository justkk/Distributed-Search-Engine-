package edu.upenn.cis.cis455.m1.server.http;

/***
 * Repository of different Configurations.
 * Routes
 * Filters
 * File Fetch
 */

public class MatchConfiguration {


    private RoutesHandlerConfiguration routesHandlerConfiguration;

    private StaticFileConfiguration staticFileConfiguration;

    private RoutesFilterConfiguration routesFilterConfiguration;


    public MatchConfiguration(RoutesFilterConfiguration routesFilterConfiguration,
                              RoutesHandlerConfiguration routesConfiguration, StaticFileConfiguration staticFileConfiguration) {

        this.routesHandlerConfiguration = routesConfiguration;
        this.staticFileConfiguration = staticFileConfiguration;
        this.routesFilterConfiguration = routesFilterConfiguration;

    }


    public RoutesHandlerConfiguration getRoutesHandlerConfiguration() {
        return routesHandlerConfiguration;
    }

    public void setRoutesHandlerConfiguration(RoutesHandlerConfiguration routesHandlerConfiguration) {
        this.routesHandlerConfiguration = routesHandlerConfiguration;
    }

    public RoutesFilterConfiguration getRoutesFilterConfiguration() {
        return routesFilterConfiguration;
    }

    public void setRoutesFilterConfiguration(RoutesFilterConfiguration routesFilterConfiguration) {
        this.routesFilterConfiguration = routesFilterConfiguration;
    }

    public StaticFileConfiguration getStaticFileConfiguration() {
        return staticFileConfiguration;
    }

    public void setStaticFileConfiguration(StaticFileConfiguration staticFileConfiguration) {
        this.staticFileConfiguration = staticFileConfiguration;
    }
}
