package com.thoughtworks.gauge.test.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.StepImpl;

public class UnknownProject extends GaugeProject {
    public UnknownProject(String language, String projName) throws IOException {
        super(language, projName);
    }

    @Override
    public void implementStep(StepImpl stepImpl) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Map<String, String> getLanguageSpecificFiles() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getLanguageSpecificGitIgnoreText() {
        return new ArrayList<>();
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createHookWithPrint(String hookLevel, String hookType, String implementation) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createHookWithException(String hookLevel, String hookType) throws IOException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getDataStoreWriteStatement(TableRow row, List<String> columnNames) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getDataStorePrintValueStatement(TableRow row, List<String> columnNames) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void configureCustomScreengrabber(String stubScreenshot) throws IOException {

    }
}
