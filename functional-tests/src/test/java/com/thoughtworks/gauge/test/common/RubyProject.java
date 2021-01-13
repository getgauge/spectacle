package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.StepImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RubyProject extends GaugeProject {
    private static final String DEFAULT_AGGREGATION = "AND";
    private static final Boolean BUNDLE_INSTALL_LOCK = false;

    public RubyProject(String projName) throws IOException {
        super("ruby", projName);
    }

    private StringBuilder createStepTeplate(ArrayList<String> stepTexts) {
        StringBuilder step = new StringBuilder();
        if (stepTexts.size() == 1) {
            return step.append("step '").append(stepTexts.get(0)).append("'");
        } else {
            StringBuilder commaSeparated = new StringBuilder();
            for (int i = 0; i < stepTexts.size(); i++) {
                commaSeparated.append("\"").append(stepTexts.get(i)).append("\"");
                if (i != stepTexts.size() - 1) commaSeparated.append(",");
            }
            return step.append("step ").append(commaSeparated);
        }
    }

    @Override
    public boolean initialize(boolean remoteTemplate) throws Exception {
        if (!remoteTemplate) {
            String gauge_project_root = System.getenv("GAUGE_PROJECT_ROOT");
            Path templatePath = Paths.get(gauge_project_root, "resources", "LocalTemplates", "ruby");
            try {
                synchronized (BUNDLE_INSTALL_LOCK) {
                    Files.createFile(Paths.get(templatePath.toString(), ".buildlock"));
                    String osName = System.getProperty("os.name").toLowerCase();
                    String bundleCommand = osName.contains("win") ? "bundle.bat" : "bundle";
                    ProcessBuilder processBuilder = new ProcessBuilder(bundleCommand, "install", "--path=vendor/bundle");
                    processBuilder.directory(templatePath.toFile());
                    processBuilder.environment().remove("CLASSPATH");
                    Process process = processBuilder.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    String newLine = System.getProperty("line.separator");
                    lastProcessStdout = "";
                    while ((line = br.readLine()) != null) {
                        lastProcessStdout = lastProcessStdout.concat(line).concat(newLine);
                    }
                    lastProcessStderr = "";
                    br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = br.readLine()) != null) {
                        lastProcessStderr = lastProcessStderr.concat(line).concat(newLine);
                    }
                    process.waitFor();
                    if (process.exitValue() != 0)
                        return false;
                }
            } catch (IOException ex) {
            }
        }
        return super.initialize(remoteTemplate);
    }

    @Override
    public void implementStep(StepImpl stepImpl) throws Exception {
        List<String> paramTypes = new ArrayList<String>();
        StepValueExtractor stepValueExtractor = new StepValueExtractor();
        StepValueExtractor.StepValue stepValue = stepValueExtractor.getFor(stepImpl.getFirstStepText());

        String fileName = Util.getUniqueName();
        StringBuilder rubyCode = new StringBuilder();
        rubyCode.append(createStepTeplate(stepValueExtractor.getValueFor(stepImpl.getAllStepTexts())));
        if (stepImpl.isContinueOnFailure()) {
            rubyCode.append(", :continue_on_failure => true");
        }
        rubyCode.append(" do |");
        for (int i = 0; i < stepValue.paramCount; i++) {
            if (i + 1 == stepValue.paramCount) {
                rubyCode.append("param").append(i);
            } else {
                rubyCode.append("param").append(i).append(", ");
            }
            paramTypes.add("string");
        }
        rubyCode.append("|\n").append(getStepImplementation(stepValue, stepImpl.getImplementation(), paramTypes, stepImpl.isValidStatement())).append("\nend");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), fileName + ".rb"), rubyCode.toString());
    }

    @Override
    public Map<String, String> getLanguageSpecificFiles() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("step_implementations", "dir");
        map.put(Util.combinePath("step_implementations", "step_implementation.rb"), "file");
        map.put(Util.combinePath("env", "default", "ruby.properties"), "file");
        return map;
    }

    @Override
    public List<String> getLanguageSpecificGitIgnoreText() {
        return new ArrayList<String>() {
            private static final long serialVersionUID = 1474926400613083856L;

            {
            add("# Gauge - metadata dir\n.gauge");
            add("# Gauge - log files dir\nlogs");
            add("# Gauge - reports generated by reporting plugins\nreports\n\n");
            add("# Gauge - ruby dependencies dir\nvendor\n");
        }};
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        StringBuilder builder = new StringBuilder();
        if (implementation.equalsIgnoreCase(PRINT_PARAMS)) {
            builder.append("puts ");
            for (int i = 0; i < stepValue.paramCount; i++) {
                builder.append("\"param").append(i).append("=\"+").append("param").append(i).append(".to_s");
                if (i != stepValue.paramCount - 1) {
                    builder.append("+\",\"+");
                }
            }
            builder.append("\n");
        } else if (implementation.equalsIgnoreCase(THROW_EXCEPTION)) {
            builder.append("raise \" exception raised \" \n \n");
        } else if (implementation.equalsIgnoreCase(CAPTURE_SCREENSHOT)) {
            builder.append("Gauge.capture\n \n");
        } else {
            if (appendCode) {
                builder.append(implementation);
            } else {
                builder.append("puts ").append(implementation).append(".to_s").append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public void createHookWithPrint(String hookLevel, String hookType, String printString) throws IOException {
        createHook(hookLevel, hookType, printString, DEFAULT_AGGREGATION, new ArrayList<String>());
    }

    @Override
    public void createHookWithException(String hookLevel, String hookType) throws IOException {
        StringBuilder rubyFileText = new StringBuilder();
        rubyFileText.append(String.format("%s_%s do \n raise \"exception was raised\"\nend", hookType, hookLevel));
        rubyFileText.append("\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".rb"), rubyFileText.toString());
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException {
        createHook(hookLevel, hookType, printString, aggregation, tags.getColumnValues("tags"));
    }

    public void createHook(String hookLevel, String hookType, String printString, String aggregation, List<String> tags) throws IOException {
        StringBuilder rubyFileText = new StringBuilder();
        rubyFileText.append(String.format("%s_%s(%s) do \n puts \"%s\"\nend", hookType, hookLevel, getOptions(aggregation, tags), printString));
        rubyFileText.append("\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".rb"), rubyFileText.toString());
    }

    @Override
    public String getDataStoreWriteStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        String value = row.getCell("value");
        return "Gauge::DataStoreFactory." + dataStoreType.toLowerCase() + "_datastore.put(\"" + key + "\", \"" + value + "\")";
    }

    private String getOptions(String aggregation, List<String> tags) {
        String tagsText = Util.joinList(Util.quotifyValues(tags));
        return String.format("{tags: [%s], operator: '%s'}", tagsText, aggregation);
    }

    @Override
    public String getDataStorePrintValueStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        return "puts Gauge::DataStoreFactory." + dataStoreType.toLowerCase() + "_datastore.get(\"" + key + "\")";
    }

    @Override
    public void configureCustomScreengrabber(String screenshotFile) throws IOException {
        StringBuilder rubyFileText = new StringBuilder();
        rubyFileText.append("Gauge.configure {|c| c.custom_screenshot_writer =  -> { return \"" + screenshotFile + "\" }}");
        rubyFileText.append("\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".rb"), rubyFileText.toString());
    }

    private String getStepImplementationsDir() {
        return new File(getProjectDir(), "step_implementations").getAbsolutePath();
    }
}
