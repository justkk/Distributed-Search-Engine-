package edu.upenn.cis.cis455.crawler;

public enum DocType {
    HTML,
    XML,
    OTHER;

    public static DocType getTypeFromContentType(String contentType) {
        if(contentType == null) {
            return null;
        }
        String[] entries = contentType.split(";");
        String mainType = entries[0];
        if("text/html".equals(mainType.toLowerCase())) {
            return HTML;
        } else if ("application/xml".equals(mainType.toLowerCase())) {
            return XML;
        } else if("text/xml".equals(mainType.toLowerCase())) {
            return XML;
        }
        else if(mainType.toLowerCase().endsWith("+xml")) {
            return XML;
        }
        return OTHER;
    }
}
