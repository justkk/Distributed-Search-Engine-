package edu.upenn.cis.cis455.xpathengine.utils;

import edu.upenn.cis.cis455.xpathengine.models.XPathElement;
import edu.upenn.cis.cis455.xpathengine.models.XPathFilters;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPathParser {

    public static List<XPathElement> getXPathElementsFromQuery(String query) {

        if(query == null || query.equals("")) {
            return null;
        }
        String[] elementList = query.split("/(?![^\"]*\"(?:(?:[^\"]*\"){2})*[^\"]*$)");

        int elementIndex = 1;
        List<XPathElement> xPathElementList = new ArrayList<>();
        while(elementIndex < elementList.length) {

            String elementQuery = elementList[elementIndex].trim();
            XPathElement xPathElement = getXPathElementFromElementQuery(elementQuery);
            if(xPathElement == null) {
                return null;
            }
            xPathElementList.add(xPathElement);
            elementIndex += 1;
        }

        return xPathElementList;
    }

    public static XPathElement getXPathElementFromElementQuery(String elementQuery) {

        if(elementQuery.equals("") || elementQuery.equals("*")) {
            XPathElement xPathElement = new XPathElement();
            xPathElement.setElementType(XPathElement.ElementType.getElementTypeFromString(elementQuery));
            xPathElement.setxPathFiltersList(new ArrayList<>());
            xPathElement.setElementName(elementQuery);
            return xPathElement;
        }
        XPathElement xPathElement = new XPathElement();

        String tmp[] = elementQuery.split("\\[", 2);
        String attrQuery = "";
        String elementName = tmp[0].trim();
        if(tmp.length >= 2) {
            attrQuery = tmp[1].trim();
            attrQuery = attrQuery.substring(0, attrQuery.length()-1);
        }
        String list[] = attrQuery.split("(\\]\\[)(?![^\"]*\"(?:(?:[^\"]*\"){2})*[^\"]*$)");
        xPathElement.setElementName(elementName);
        xPathElement.setElementType(XPathElement.ElementType.NORMAL);
        List<XPathFilters> xPathFiltersList = new ArrayList<>();
        int attrIndex = 0;
        while (attrIndex < list.length) {

            String attr = list[attrIndex];
            if(attr.equals("")) {
                attrIndex += 1;
                continue;
            }

            attr = attr.trim();

            String matchRegex1="text *?\\( *?\\) *?= *?\"(.*)\"";
            String matchRegex2="contains *?\\( *?text *?\\( *?\\) *?, *?\"(.*)\" *?\\)";

            Map<String, XPathFilters.FilterType> filterTypeMap = new HashMap<>();
            filterTypeMap.put(matchRegex1, XPathFilters.FilterType.EXACT_MATCH);
            filterTypeMap.put(matchRegex2, XPathFilters.FilterType.CONTAINS);

            XPathFilters xPathFilter = null;

            for (Map.Entry<String, XPathFilters.FilterType> entry : filterTypeMap.entrySet()) {
                String value = getValueFromRegexMatching(entry.getKey(), attr);
                if(value == null) {
                    continue;
                } else {
                    xPathFilter = new XPathFilters(entry.getValue(), value);
                    break;
                }
            }

            if(xPathFilter == null) {
                return null;
            }

            xPathFiltersList.add(xPathFilter);
            attrIndex += 1;
        }
        xPathElement.setxPathFiltersList(xPathFiltersList);
        return xPathElement;
    }

    public static String getValueFromRegexMatching(String patternString, String inputString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(inputString);
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void main(String[] args) {

        List<XPathElement> answer = getXPathElementsFromQuery("/d/e/f/foo[text()=\"something\"][text()=\"nothing\"]/bar");
        System.out.println(answer);

    }
}
