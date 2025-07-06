package com.tuum.common.types;

public enum RequestType {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete");

    private final String code;

    RequestType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static RequestType fromCode(String code) {
        for (RequestType type : RequestType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RequestType code: " + code);
    }
}

