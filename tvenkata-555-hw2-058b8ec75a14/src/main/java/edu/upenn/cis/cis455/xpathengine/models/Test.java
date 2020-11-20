package edu.upenn.cis.cis455.xpathengine.models;

import edu.upenn.cis.cis455.xpathengine.utils.XPathRecursiveParser;

public class Test {

    TestType testType;
    String quotedString;

    public Test(TestType testType, String quotedString) {
        this.testType = testType;
        this.quotedString = quotedString;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public String getQuotedString() {
        return quotedString;
    }

    public void setQuotedString(String quotedString) {
        this.quotedString = quotedString;
    }
}
