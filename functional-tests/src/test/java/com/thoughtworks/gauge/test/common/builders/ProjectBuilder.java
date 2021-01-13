package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.Gauge;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Util;

public class ProjectBuilder {

    private String language;
    private String projName;
    private boolean deleteExampleSpec;
    private boolean remoteTemplate;

    public ProjectBuilder() {
        this.remoteTemplate = false;
    }

    public ProjectBuilder withLangauge(String language) {
        this.language = language;
        return this;
    }

    public ProjectBuilder withProjectName(String projName) {
        this.projName = projName;
        return this;
    }

    public ProjectBuilder withRemoteTemplate() {
        this.remoteTemplate = true;
        return this;
    }

    public GaugeProject build(boolean expectFailure) throws Exception {
        GaugeProject currentProject = GaugeProject.createProject(language, projName);
        if (!currentProject.initialize(remoteTemplate) && !expectFailure)
            throw new Exception("Unable to initialize gauge project.\nSTDERR:\n\n"
                    + currentProject.getLastProcessStderr() + "\n\nSTDOUT:\n\n"
                    + currentProject.getLastProcessStdout());

        if (this.deleteExampleSpec)
            currentProject.deleteSpec(Util.combinePath("specs", "example"));

        Gauge.writeMessage("STDOUT\n" + currentProject.getLastProcessStdout());
        Gauge.writeMessage("STDERR\n" + currentProject.getLastProcessStderr());
        return currentProject;
    }

    public ProjectBuilder withoutExampleSpec() {
        this.deleteExampleSpec = true;
        return this;
    }
}
