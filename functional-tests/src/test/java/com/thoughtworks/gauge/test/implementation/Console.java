package com.thoughtworks.gauge.test.implementation;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import com.thoughtworks.gauge.Step;

public class Console {

    @Step({"Console should contain <message>", "The error message <message> should be displayed on console"})
    public void consoleShouldContain(String message) throws IOException {
        String output = getCurrentProject().getStdOut();
        assertThat(output).contains(message);
    }
}
