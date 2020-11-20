package edu.upenn.cis.cis455.xpathengine.models;

public class XPathFilters {

    private FilterType filterType;
    private String matchingValue;

    public XPathFilters(FilterType filterType, String matchingValue) {
        this.filterType = filterType;
        this.matchingValue = matchingValue;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public String getMatchingValue() {
        return matchingValue;
    }

    public void setMatchingValue(String matchingValue) {
        this.matchingValue = matchingValue;
    }

    public enum FilterType {
        EXACT_MATCH,
        CONTAINS;

    }

    public static FilterType getFilterType(TestType testType) {
        if (testType == TestType.EXACT_MATCH) {
            return FilterType.EXACT_MATCH;
        }
        return FilterType.CONTAINS;
    }
}
