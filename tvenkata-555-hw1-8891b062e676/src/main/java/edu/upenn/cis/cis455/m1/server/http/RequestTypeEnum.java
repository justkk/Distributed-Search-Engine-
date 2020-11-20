package edu.upenn.cis.cis455.m1.server.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum RequestTypeEnum {

    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    PUT("PUT"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    private final String type;
    private static Map<String, RequestTypeEnum> enumMap = new HashMap<>();

    RequestTypeEnum(final String type) {
        this.type = type;
    }

    static {
        Arrays.stream(RequestTypeEnum.values()).forEach(enumType -> {
            enumMap.put(enumType.type, enumType);
        });

    }

    public String getType() {
        return type;
    }

    public static RequestTypeEnum getEnumFromString(String type) {

        if(type == null) {
            return null;
        }

        String key = type.toUpperCase();
        if (!enumMap.containsKey(key)) {
            return null;
        }
        return enumMap.get(key);

    }


}
