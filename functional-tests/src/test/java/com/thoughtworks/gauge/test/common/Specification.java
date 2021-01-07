package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Specification {

    private String name;
    private List<Scenario> scenarios = new ArrayList<Scenario>();
    private List<String> contextSteps = new ArrayList<String>();
    private List<String> teardownSteps = new ArrayList<String>();
    private File specFile = null;
    private Table datatable = null;
    private String tags = "";

    public Specification(String name) {
        this.name = name;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public String getName() {
        return name;
    }

    public void addScenarios(boolean dedup, Scenario... newScenarios) {
        if (!dedup) {
            Collections.addAll(scenarios, newScenarios);
            return;
        }
        List<Scenario> toBeRemoved = new ArrayList<>();
        for (Scenario newScenario : newScenarios) {
            scenarios.forEach(scenario -> {
                if (scenario.getName().equalsIgnoreCase(newScenario.getName())) {
                    toBeRemoved.add(scenario);
                }
            });
        }
        scenarios.removeAll(toBeRemoved);
        Collections.addAll(scenarios, newScenarios);
    }

    public void addContextSteps(String... newContextSteps) {
        Collections.addAll(contextSteps, newContextSteps);
    }

    public void addTeardownSteps(String... newTeardownSteps) {
        Collections.addAll(teardownSteps, newTeardownSteps);
    }

    public void addDataTable(Table datatable) {
        this.datatable = datatable;
    }

    @Override
    public String toString() {
        StringBuilder specText = new StringBuilder();
        specText.append("# ").append(name).append("\n\n");
        specText.append("tags: ").append(this.tags).append("\n");
        if (datatable != null) {
            specText.append(tableString(datatable));
        }
        for (String contextStep : contextSteps) {
            specText.append("* ").append(contextStep).append("\n");
        }
        specText.append("\n");

        for (Scenario scenario : scenarios) {
            specText.append("## ").append(scenario.getName()).append("\n\n");
            for (String tag : scenario.getTags()) {
                specText.append("tags: ").append(tag).append("\n");
            }
            for (Item item : scenario.getItems()) {
                specText.append(item.toString()).append("\n");
            }
            specText.append("\n");
        }

        specText.append("\n");
        specText.append("_____\n\n");
        for (String step : teardownSteps) {
            specText.append("* ").append(step).append("\n");
        }
        return specText.toString();
    }

    private String tableString(Table datatable) {
        StringBuilder builder = new StringBuilder();
        List<String> columnNames = datatable.getColumnNames();
        appendRow(builder, columnNames);
        for (TableRow tableRow : datatable.getTableRows()) {
            ArrayList<String> row = new ArrayList<String>();
            for (String column : columnNames) {
                row.add(tableRow.getCell(column));
            }
            appendRow(builder, row);
        }
        return builder.toString();
    }

    private void appendRow(StringBuilder builder, List<String> row) {
        for (int i = 0; i < row.size(); i++) {
            String rowItem = row.get(i);
            builder.append("|").append(rowItem);
            if (i == row.size() - 1) {
                builder.append("|\n");
            }
        }
    }

    public void saveAs(File file) throws IOException {
        Util.writeToFile(file.getAbsolutePath(), this.toString());
        this.specFile = file;
    }

    public void save() throws IOException {
        if (specFile == null) {
            throw new RuntimeException("Don't know where to save the spec to");
        }
        saveAs(specFile);
    }

    public File getSpecFile() {
        return specFile;
    }

    public void addTags(String tags) {
        this.tags = tags;
    }
}
