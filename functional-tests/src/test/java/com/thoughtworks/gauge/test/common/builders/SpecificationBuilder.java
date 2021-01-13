package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.common.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;

public class SpecificationBuilder {
    private ScenarioBuilder scenarioBuilder;
    private TagsBuilder tagsBuilder;
    private ContextBuilder contextBuilder;
    private TeardownBuilder teardownBuilder;

    private String specName;
    private String subDirPath;
    private Table datatable;

    public SpecificationBuilder() {
        scenarioBuilder = new ScenarioBuilder();
        tagsBuilder = new TagsBuilder();
        contextBuilder = new ContextBuilder();
        teardownBuilder = new TeardownBuilder();
    }

    public SpecificationBuilder withContextSteps(Table contextSteps) {
        this.contextBuilder.withContextSteps(contextSteps);
        return this;
    }

    public SpecificationBuilder withScenarioName(String scenarioName) {
        this.tagsBuilder.withScenarioName(scenarioName);
        this.scenarioBuilder.withScenarioName(scenarioName);
        return this;
    }

    public SpecificationBuilder withSubDirPath(String subDirPath) {
        this.subDirPath = subDirPath;
        return this;
    }

    public SpecificationBuilder withSpecName(String specName) {
        this.specName = specName;
        return this;
    }

    public SpecificationBuilder withSteps(Table steps) {
        this.scenarioBuilder.withSteps(steps);
        return this;
    }

    public SpecificationBuilder withAppendCode(boolean appendCode) {
        this.contextBuilder.withAppendCode(appendCode);
        this.teardownBuilder.withAppendCode(appendCode);
        this.scenarioBuilder.withAppendCode(appendCode);
        return this;
    }

    public SpecificationBuilder withTeardownSteps(Table tearDownSteps) {
        this.teardownBuilder.withTeardownSteps(tearDownSteps);
        return this;
    }

    public void buildAndAddToProject(boolean dedupScenario) throws Exception {
        Specification spec = getCurrentProject().findSpecification(specName);
        if (spec == null) {
            spec = getCurrentProject().createSpecification(subDirPath, specName);
        }

        contextBuilder.withSpecification(spec);
        teardownBuilder.withSpecification(spec);
        scenarioBuilder.withSpecification(spec);
        tagsBuilder.withSpecification(spec);

        if (datatable != null)
            spec.addDataTable(datatable);

        if (contextBuilder.canBuild())
            contextBuilder.build();

        if (scenarioBuilder.canBuild())
            scenarioBuilder.build(dedupScenario);

        if (tagsBuilder.canBuild())
            tagsBuilder.build();

        if (teardownBuilder.canBuild())
            teardownBuilder.build();

        spec.save();
    }

    public SpecificationBuilder withTags(String tags) {
        this.tagsBuilder.withTags(tags);
        return this;
    }

    public SpecificationBuilder withDataTable(Table datatable) {
        this.datatable = datatable;
        return this;
    }

    public SpecificationBuilder withScenarioDataStoreWriteStatement(String key, String value) {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("key");
        columns.add("value");

        TableRow row = new TableRow();
        row.addCell("key", key);
        row.addCell("value", value);
        row.addCell("datastore type", "Scenario");

        getCurrentProject().getDataStoreWriteStatement(row, columns);
        return this;
    }

    public SpecificationBuilder withDataStoreWriteStatement(List<String> columnNames, TableRow row) {
        scenarioBuilder.addSteps(columnNames, row.getCell("step text"), getCurrentProject().getDataStoreWriteStatement(row, columnNames));
        return this;
    }

    public SpecificationBuilder withDataStorePrintValues(List<String> columnNames, TableRow tableRow) {
        scenarioBuilder.addSteps(columnNames, tableRow.getCell("step text"), getCurrentProject().getDataStorePrintValueStatement(tableRow, columnNames));
        return this;
    }

}
