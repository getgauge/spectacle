package com.thoughtworks.gauge.test.common;

public class Step implements Item {
    public static String TYPE = "step";
    private String value;

    public Step(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "* " + value;
    }
}
