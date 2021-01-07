package com.thoughtworks.gauge.test.common;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;

import java.io.File;
import java.io.IOException;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;

import org.apache.commons.io.FileUtils;

public class Hooks {
    @AfterScenario
    public void tearDown() {
        if (getCurrentProject() == null) {
            System.out.println("Current project is unavailable");
            return;
        }
        File dir = getCurrentProject().getProjectDir();
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            System.out.println(String.format("Could not delete project directory %s; reason : %s", dir.getAbsolutePath(), e.getMessage()));
        }
    }

    @BeforeScenario
    public void setProjectName(ExecutionContext context) {
        String folderName = Util.combinePath(context.getCurrentSpecification().getName().replaceAll(" ", "_"),
                context.getCurrentScenario().getName().replaceAll(" ", "_"));
        ScenarioDataStore.put("log_proj_name", folderName);
    }
}
