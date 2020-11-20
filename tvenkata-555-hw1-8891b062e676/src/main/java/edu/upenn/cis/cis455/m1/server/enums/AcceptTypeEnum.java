package edu.upenn.cis.cis455.m1.server.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/***
 *
 * Enum for accept-type
 */

public enum AcceptTypeEnum {

    APPLICATION_ATOM_XML("application/atom+xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_SVG_XML("application/svg+xml"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    MEDIA_TYPE_WILDCARD("*"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    WILDCARD("*/*");

    private String content;
    private static Map<String, AcceptTypeEnum> stringHttpContentTypeEnumMap = new HashMap<>();

    AcceptTypeEnum(String content) {
        this.content = content;
    }


    static {
        Arrays.stream(AcceptTypeEnum.values()).forEach(httpContentTypeEnum -> {
            stringHttpContentTypeEnumMap.put(httpContentTypeEnum.getContent(), httpContentTypeEnum);
        });

    }

    public String getContent() {
        return this.content;
    }

    public static AcceptTypeEnum getContentTypeFromString(String type) {
        if (type == null) {
            return null;
        }
        if (!stringHttpContentTypeEnumMap.containsKey(type)) {
            return null;
        }
        return stringHttpContentTypeEnumMap.get(type);
    }


}
