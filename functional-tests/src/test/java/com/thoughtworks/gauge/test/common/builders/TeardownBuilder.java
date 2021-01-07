package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Specification;

public class TeardownBuilder {
    private Table tearDownSteps;
    private boolean appendCode;
    private Specification specification;

    public TeardownBuilder withTeardownSteps(Table tearDownSteps) {
        this.tearDownSteps = tearDownSteps;
        return this;
    }

    public TeardownBuilder withAppendCode(boolean appendCode) {
        this.appendCode = appendCode;
        return this;
    }

    public boolean canBuild() {
        return tearDownSteps != null;
    }

    public TeardownBuilder withSpecification(Specification spec) {
        this.specification = spec;
        return this;
    }

    public void build() throws Exception {
        if (!canBuild())
            throw new Exception("needed teardown steps");
        for (TableRow row : tearDownSteps.getTableRows()) {
            specification.addTeardownSteps(row.getCell("step text"));
            GaugeProject.implement(tearDownSteps, row, appendCode);
        }
    }
}
