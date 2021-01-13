package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Specification;

public class ContextBuilder {
    private Table contextSteps;
    private boolean appendCode;
    private Specification specification;

    public void build() throws Exception {
        if (!canBuild()) {
            throw new Exception("need context steps");
        }
        for (TableRow row : contextSteps.getTableRows()) {
            specification.addContextSteps(row.getCell("step text"));
            GaugeProject.implement(contextSteps, row, appendCode);
        }
    }

    public boolean canBuild() {
        return contextSteps != null;
    }

    public ContextBuilder withSpecification(Specification spec) {
        this.specification = spec;
        return this;
    }

    public ContextBuilder withAppendCode(boolean appendCode) {
        this.appendCode = appendCode;
        return this;
    }

    public ContextBuilder withContextSteps(Table contextSteps) {
        this.contextSteps = contextSteps;
        return this;
    }
}
