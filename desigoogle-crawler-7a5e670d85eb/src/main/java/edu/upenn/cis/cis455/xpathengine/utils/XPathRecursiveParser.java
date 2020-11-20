package edu.upenn.cis.cis455.xpathengine.utils;

import edu.upenn.cis.cis455.xpathengine.models.Step;
import edu.upenn.cis.cis455.xpathengine.models.Test;
import edu.upenn.cis.cis455.xpathengine.models.TestType;
import edu.upenn.cis.cis455.xpathengine.models.XPathQueryNode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPathRecursiveParser {

    private String inputString;
    private int d_currOffset;
    private boolean wildTail = false;
    private String originalString;

    public XPathRecursiveParser(String inputString) {
        this.inputString = inputString.trim();
        this.d_currOffset = 0;
        this.originalString = inputString;
    }



    private void consume(int numChar) {
        inputString = inputString.substring(numChar);
        d_currOffset += numChar;
    }

    private void consumeWhitespace() {
        while (inputString.length() > 0
                && Character.isWhitespace(inputString.charAt(0))) {
            consume(1);
        }
    }

    private void reset(int beginIndex) {
        inputString = originalString.substring(beginIndex);
    }


    public String getNodeNameExtractor() throws ParseException {

        Pattern p = Pattern.compile("^[a-zA-Z_][a-z0-9A-Z_\\.\\:-]*");
        Matcher m = p.matcher(inputString);
        int startIndex = d_currOffset;
        if (m.find()) {

            int startIdx = m.start();
            int endIdx = m.end();
            if (startIdx != 0) {
                reset(startIndex);
                throw new ParseException("Expected node name not space", d_currOffset);
            }
            // Parse and return the quoted string
            String nodeName = inputString.substring(startIdx, endIdx);
            consume(nodeName.length());
            return nodeName;

        } else {
            reset(startIndex);
            if(inputString.charAt(0) == '*') {
                consume(1);
                return "*";
            }
            throw new ParseException("node name not found", d_currOffset);
        }
    }


    public List<Test> testExtractorList() throws ParseException {

        List<Test> testList = new ArrayList<>();
        int startIndex = d_currOffset;

        while (true) {

            if (inputString.equals("") || inputString.length() == 0) {
                return testList;
            }
            if (inputString.charAt(0) == '/') {
                return testList;
            }
            if (inputString.charAt(0) != '[') {
                reset(startIndex);
                throw new ParseException("Illegeal state", d_currOffset);
            }
            consume(1);
            consumeWhitespace();
            Test test = testExtractor();
            consumeWhitespace();
            if (inputString.charAt(0) != ']') {
                reset(startIndex);
                throw new ParseException("Illegeal state", d_currOffset);
            }
            consume(1);
            testList.add(test);
            consumeWhitespace();
        }

    }

    public Test testExtractor() throws ParseException {

        Test test = null;
        int startIndex = d_currOffset;
        try {
            test = testExtractor1();
        } catch (ParseException p) {
            try {
                test = testExtractor2();
            } catch (ParseException e) {
                reset(startIndex);
                throw new ParseException("No test match found", d_currOffset);
            }
        }
        return test;
    }

    public Test testExtractor1() throws ParseException {

        String matchRegex1 = "text *?\\( *?\\) *?= *?";
        Pattern p = Pattern.compile(matchRegex1);
        Matcher m = p.matcher(inputString);
        int startIndex = d_currOffset;
        if (m.find()) {
            int startIdx = m.start();
            int endIdx = m.end();
            if (startIdx != 0) {
                reset(startIndex);
                throw new ParseException("Expected Quoted String", d_currOffset);
            }
            String testString = inputString.substring(startIdx, endIdx);
            consume(testString.length());
            consumeWhitespace();
            String quotedString = quotedStringExtractor();
            return new Test(TestType.EXACT_MATCH, quotedString);

        } else {
            reset(startIndex);
            throw new ParseException("test1 failed", d_currOffset);
        }

    }

    public Test testExtractor2() throws ParseException {

        String matchRegex2 = "contains *?\\( *?text *?\\( *?\\) *?, *?";

        Pattern p = Pattern.compile(matchRegex2);
        Matcher m = p.matcher(inputString);
        int startIndex = d_currOffset;
        if (m.find()) {
            int startIdx = m.start();
            int endIdx = m.end();
            if (startIdx != 0) {
                reset(startIndex);
                throw new ParseException("Expected Quoted String", d_currOffset);
            }
            String testString = inputString.substring(startIdx, endIdx);
            consume(testString.length());
            consumeWhitespace();
            String quotedString = quotedStringExtractor();
            consumeWhitespace();
            if (inputString.length() == 0 || inputString.charAt(0) != ')') {
                reset(startIndex);
                throw new ParseException("test2 failed", d_currOffset);
            }
            consume(1);
            return new Test(TestType.SUBSTRING_MATCH, quotedString);

        } else {
            reset(startIndex);
            throw new ParseException("test2 failed", d_currOffset);
        }

    }


    public void slashExtractor() throws ParseException {

        int startIndex = d_currOffset;

        if (inputString.charAt(0) != '/') {
            reset(startIndex);
            throw new ParseException("Slash not found", d_currOffset);
        }
        consumeWhitespace();
        if (inputString.length() > 1 && inputString.charAt(1) == '/') {
            consume(1);
            wildTail = true;
        }
        consume(1);
    }

    public Step getStepExtractor() throws ParseException {

        String nodeName = getNodeNameExtractor();
        consumeWhitespace();
        List<Test> testList = testExtractorList();
        Step step = new Step(nodeName, testList, wildTail);
        wildTail = false;
        return step;
    }

    public List<Step> xPathExtractor() throws ParseException {

        List<Step> stepList = new ArrayList<>();
        do {
            consumeWhitespace();
            slashExtractor();
            consumeWhitespace();
            Step step = getStepExtractor();
            stepList.add(step);
        } while (inputString.length() != 0);

        return stepList;
    }


    public String quotedStringExtractor() throws ParseException {

        Pattern p = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
        Matcher m = p.matcher(inputString);
        int startIndex = d_currOffset;
        if (m.find()) {
            int startIdx = m.start();
            int endIdx = m.end();

            if (startIdx != 0) {
                reset(startIndex);
                throw new ParseException("Expected Quoted String", d_currOffset);
            }
            // Parse and return the quoted string
            String quotedString = inputString.substring(startIdx + 1, endIdx - 1);
            consume(quotedString.length() + 2);
            return quotedString;

        } else {
            reset(startIndex);
            throw new ParseException("Quoted String not found", d_currOffset);
        }
    }


    public static void main(String[] args) throws ParseException {
        XPathRecursiveParser xPathRecursiveParser = new XPathRecursiveParser("/html/h2/*/c[text() =\"whiteSpaces hould NotMatter\"]");
        List<Step> stepList = xPathRecursiveParser.xPathExtractor();
        System.out.println(stepList);
        List<XPathQueryNode> xPathQueryNodeList = XPathQueryNode.getQueryNodesFromStepNode(stepList, "q1");
        System.out.println(xPathQueryNodeList);

    }


}
