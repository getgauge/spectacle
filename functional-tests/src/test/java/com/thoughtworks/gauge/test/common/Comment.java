package com.thoughtworks.gauge.test.common;

public class Comment implements Item {
    public static String TYPE = "comment";
    private String value;

    public Comment(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
