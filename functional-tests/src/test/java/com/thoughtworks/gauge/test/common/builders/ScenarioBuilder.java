package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Scenario;
import com.thoughtworks.gauge.test.common.Specification;

import java.util.Arrays;
import java.util.List;

public class ScenarioBuilder {

    private String scenarioName;
    private Table scenarioSteps;
    private boolean appendCode;
    private Specification spec;

    public ScenarioBuilder withScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
        return this;
    }

    public ScenarioBuilder withSteps(Table steps) {
        this.scenarioSteps = steps;
        return this;
    }

    public ScenarioBuilder withAppendCode(boolean appendCode) {
        this.appendCode = appendCode;
        return this;
    }

    private Scenario buildScenario() throws Exception {
        if (!canBuild())
            throw new Exception("scenario name and steps needed for initialization");

        Scenario scenario = new Scenario(scenarioName);
        for (TableRow row : scenarioSteps.getTableRows()) {
            scenario.addItem(getStepText(row.getCell("step text"), row), row.getCell("Type"));
            GaugeProject.implement(scenarioSteps, row, appendCode);
        }

        return scenario;
    }

    private static String getStepText(String stepText, TableRow row) {
        int numberOfHeaders = row.getCell("inlineTableHeaders").split(",").length;
        String headers = "|" + (row.getCell("inlineTableHeaders").replaceAll(",", "|")) + "|";

        if (headers == "||")
            return stepText;

        StringBuilder tableContent = new StringBuilder("\n" + headers);

        for (int index = 1; index <= numberOfHeaders; index++) {
            String columnValues = "|" + (row.getCell("row" + index).replaceAll(",", "|\n|")) + "|";

            if (columnValues == "||")
                break;

            tableContent.append("\n");
            tableContent.append(columnValues);
        }
        return stepText.replace("<inlineTable>", tableContent.toString());
    }

    public boolean canBuild() {
        return (scenarioName != null && scenarioSteps != null);
    }

    public ScenarioBuilder withSpecification(Specification spec) {
        this.spec = spec;
        return this;
    }

    public void build(boolean dedup) throws Exception {
        spec.addScenarios(dedup, buildScenario());
    }

    public ScenarioBuilder addSteps(List<String> columnNames, String cell, String step) {
        if (this.scenarioSteps == null)
            scenarioSteps = new Table(Arrays.asList(columnNames.get(0), "implementation"));

        scenarioSteps.addRow(Arrays.asList(cell, step));
        return this;
    }
}
