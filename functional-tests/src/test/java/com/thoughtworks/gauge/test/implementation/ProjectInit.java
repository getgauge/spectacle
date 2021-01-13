package com.thoughtworks.gauge.test.implementation;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Util;
import com.thoughtworks.gauge.test.common.builders.ProjectBuilder;

public class ProjectInit {

    private ThreadLocal<GaugeProject> currentProject = new ThreadLocal<GaugeProject>();

    @Step("Initialize a project named <projName> without example spec")
    public void projectInitWithoutHelloWorldSpec(String projName) throws Exception {
        currentProject.set(new ProjectBuilder()
                .withLangauge(Util.getCurrentLanguage())
                .withProjectName(projName)
                .withoutExampleSpec()
                .build(false));
    }
}
