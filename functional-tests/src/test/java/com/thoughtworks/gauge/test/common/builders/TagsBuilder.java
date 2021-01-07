package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.test.common.Scenario;
import com.thoughtworks.gauge.test.common.Specification;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;

public class TagsBuilder {

    private String scenarioName;
    private String tags;
    private Specification spec;

    public TagsBuilder withScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
        return this;
    }

    public TagsBuilder withTags(String tags) {
        this.tags = tags;
        return this;
    }

    public TagsBuilder withSpecification(Specification spec) {
        this.spec = spec;
        return this;
    }

    public boolean canBuild() {
        return tags != null;
    }

    public void build() throws Exception {
        if (!canBuild())
            throw new Exception("should have tags with scenarios or specs");

        if (scenarioName != null && tags != null) {
            Scenario currentScenario = getCurrentProject().findScenario(scenarioName, spec.getScenarios());
            currentScenario.addTags(tags);
        }

        if (scenarioName == null && tags != null) {
            spec.addTags(tags);
        }
    }
}
