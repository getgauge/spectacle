package com.thoughtworks.gauge.test.implementation;

import com.thoughtworks.gauge.*;
import com.thoughtworks.gauge.test.common.GaugeProject;
import com.thoughtworks.gauge.test.common.Util;
import com.thoughtworks.gauge.test.common.builders.DataFileBuilder;
import com.thoughtworks.gauge.test.common.builders.ProjectBuilder;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProjectInit {

    private ThreadLocal<GaugeProject> currentProject = new ThreadLocal<GaugeProject>();

    @Step("In an empty directory, use default initialization of a project named <projName> in language <language>")
    public void initializeProjectWithLanguage(String projName, String language) throws Exception {
        currentProject.set(new ProjectBuilder()
                .withLangauge(language)
                .withProjectName(projName)
                .build(language.equals("unknown")));
    }

    @Step("Initialize a project named <projName> with the example specs")
    public void defaultInitializationProject(String projName) throws Exception {
        currentProject.set(new ProjectBuilder()
                .withRemoteTemplate()
                .withLangauge(Util.getCurrentLanguage())
                .withProjectName(projName)
                .build(false));
    }

    @Step("Initialize a project named <projName> without example spec")
    public void projectInitWithoutHelloWorldSpec(String projName) throws Exception {
        currentProject.set(new ProjectBuilder()
                .withLangauge(Util.getCurrentLanguage())
                .withProjectName(projName)
                .withoutExampleSpec()
                .build(false));
    }

    @Step("The following file structure should be created <table>")
    public void ensureInitCreatesSpecifiedStructure(Table table) throws Exception {
        SoftAssertions softly = new SoftAssertions();
        for (TableRow row : table.getTableRows()) {
            File fileName = new File(getPathRelativeToCurrentProjectDir(row.getCell("name")));
            String fileType = row.getCell("type").toLowerCase();
            softly.assertThat(fileType).isIn("dir", "file");
            if (fileType.equals("dir")) {
                softly.assertThat(fileName).exists().isDirectory();
            } else if (fileType.equals("file")) {
                softly.assertThat(fileName).exists().isFile();
            }
        }
        softly.assertAll();
    }

    @Step("Verify language specific files are created")
    public void verifyFilesForLanguageIsCreated() {
        SoftAssertions softly = new SoftAssertions();
        Map<String, String> files = currentProject.get().getLanguageSpecificFiles();
        files.forEach((k, v) -> {
            File fileName = new File(getPathRelativeToCurrentProjectDir(k));
            String fileType = v.toLowerCase();
            softly.assertThat(fileType).isIn("dir", "file");
            if (fileType.equals("dir")) {
                softly.assertThat(fileName).exists().isDirectory();
            } else if (fileType.equals("file")) {
                softly.assertThat(fileName).exists().isFile();
            }
        });
        softly.assertAll();
    }


    @Step("Verify language specific .gitignore is created")
    public void verifyGitIngoreForLanguageIsCreated() throws IOException {
        List<String> gitignoreTexts = currentProject.get().getLanguageSpecificGitIgnoreText();
        File fileName = new File(getPathRelativeToCurrentProjectDir(".gitignore"));
        Assert.assertTrue(fileName.exists());
        String content = Util.read(fileName.getAbsolutePath());
        for (String gitignoreText : gitignoreTexts) {
            Assert.assertTrue(content.contains(gitignoreText));
        }
    }

    private String getPathRelativeToCurrentProjectDir(String path) {
        return Util.combinePath(currentProject.get().getProjectDir().getAbsolutePath(), path);
    }

    @Step("Directory <dirName> should be empty")
    public void verifyEmptyDir(String dirName) {
        File[] files = currentProject.get().getProjectDir().listFiles();
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected " + currentProject.get().getProjectDir().getPath() + " to be empty.", 0, files.length);
    }

    @Step("Create a csv file <name>")
    public void createCSV(String name) throws Exception {
        new DataFileBuilder()
                .withCsvFile(name)
                .build();
    }

    @Step("Create a csv file <name> with <content>")
    public void createCSVWithContent(String name, String content) throws Exception {
        new DataFileBuilder()
                .withCsvFile(name)
                .withContent(Arrays.asList(content.split("\n")))
                .build();
    }

    @Step("Create a txt file <name>")
    public void createTextFile(String name) throws Exception {
        new DataFileBuilder()
                .withTextFile(name)
                .build();
    }

    @Step("Add xml report plugin in manifest.json")
    public void AddXmlPlugin() throws IOException, JSONException {
        String manifest = Util.combinePath(currentProject.get().getProjectDir().getAbsolutePath(), "manifest.json");
        String json = Util.read(manifest);
        JSONObject jsonObject = new JSONObject(json);
        jsonObject.append("Plugins", "xml-report");
        Util.writeToFile(manifest, jsonObject.toString());
    }
}
