package com.thoughtworks.gauge.test.implementation;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.test.common.ExecutionSummary;
import com.thoughtworks.gauge.test.common.ExecutionSummaryAssert;

public class Execution {

    private String getFormattedProcessOutput() {
        return "\n*************** Process output start************\n" + getCurrentProject().getLastProcessStdout() + "\n*************** Process output end************\n";
    }

    private ExecutionSummaryAssert assertOn(ExecutionSummary summary, boolean result) {
        return ExecutionSummaryAssert.assertThat(summary).withFailMessage(getFormattedProcessOutput()).hasSuccess(result);
    }

    @Step("Generate Spectacle Documentation for the current project")
    public void generateSpectacleDocumentationForCurrentProject() throws Exception {
        assertOn(getCurrentProject().generateSpectacleDocumentation(), true);
    }
}
