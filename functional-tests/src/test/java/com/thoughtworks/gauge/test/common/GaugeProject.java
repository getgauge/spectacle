package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import com.thoughtworks.gauge.test.StepImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Arrays.asList;

public abstract class GaugeProject {
    private static final Boolean INITIALIZE_LOCK = true;

    private static final List<String> PRODUCT_ENVS = asList("GAUGE_ROOT", "GAUGE_HOME", "GAUGE_SOURCE_BUILD", "GAUGE_PYTHON_COMMAND");
    private static final List<String> GAUGE_ENVS = asList("gauge_custom_classpath", "overwrite_reports", "GAUGE_INTERNAL_PORT",
            "GAUGE_PROJECT_ROOT", "logs_directory", "GAUGE_DEBUG_OPTS", "GAUGE_API_PORT", "gauge_reports_dir", "screenshot_on_failure",
            "save_execution_result", "enable_multithreading", "screenshots_dir");
    private static final String PRODUCT_PREFIX = "GAUGE_";
    static final String PRINT_PARAMS = "print params";
    static final String THROW_EXCEPTION = "throw exception";
    static final String FAILING_IMPLEMENTATION = "failing implementation";
    static final String CAPTURE_SCREENSHOT = "capture screenshot";
    private static ThreadLocal<GaugeProject> currentProject = ThreadLocal.withInitial(() -> null);
    private static String executableName = "gauge";
    private static String specsDirName = "specs";
    private File projectDir;
    private String language;
    private ArrayList<Specification> specifications = new ArrayList<>();
    protected String lastProcessStdout;
    protected String lastProcessStderr;
    private static int projectCount = 0;

    protected GaugeProject(String language, String projName) throws IOException {
        this.language = language;
        currentProject.set(this);

        this.projectDir = Files.createTempDirectory(projName + projectCount++ + "_").toFile();
    }

    public static GaugeProject getCurrentProject() {
        if (currentProject == null) {
            throw new RuntimeException("Gauge project is not initialized yet");
        }
        return currentProject.get();
    }

    public static GaugeProject createProject(String language, String projName) throws IOException {
        switch (language.toLowerCase()) {
            case "java":
                return new JavaProject(projName);
            default:
                return new UnknownProject(language, projName);
        }
    }

    public boolean initialize(boolean remoteTemplate) throws Exception {
        executeGaugeCommand(new String[]{
                "config", "plugin_kill_timeout", "60000"}, null);
        if(remoteTemplate && language.equals("js")){
            return executeGaugeCommand(new String[]{"init", "-l", "debug", "js_simple"}, null);
        }

        if (remoteTemplate) {
            return executeGaugeCommand(new String[]{"init", "-l", "debug", language}, null);
        }

        if(Boolean.parseBoolean(System.getenv("cache_remote_init"))){
            return cacheAndFetchFromLocalTemplate();
        }

        return copyLocalTemplateIfExists(language) || executeGaugeCommand(new String[]{"init", "-l", "debug", language}, null);
    }

    private boolean isLocalTemplateAvaialable(String language){
        String gauge_project_root = System.getenv("GAUGE_PROJECT_ROOT");
        Path templatePath = Paths.get(gauge_project_root, "resources", "LocalTemplates", language);
        return (Files.exists(templatePath));
    }

    private boolean cacheAndFetchFromLocalTemplate() throws InterruptedException {
        synchronized(INITIALIZE_LOCK){//synchronized block
            String gauge_project_root = System.getenv("GAUGE_PROJECT_ROOT");
            Path templatePath = Paths.get(gauge_project_root, "resources", "LocalTemplates", language);

            try {
                if(isLocalTemplateAvaialable(language)) {
                    FileUtils.copyDirectory(templatePath.toFile(), this.projectDir);
                    return true;
                }
                else {
                    String projectName = language.equals("js") ? "js_simple" : language;
                    if(executeGaugeCommand(new String[]{"init", "-l","debug", projectName}, null)) {
                        FileUtils.copyDirectory(this.projectDir, templatePath.toFile());
                        return true;
                    }
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
    }


    private boolean copyLocalTemplateIfExists(String language) {
        String gauge_project_root = System.getenv("GAUGE_PROJECT_ROOT");
        Path templatePath = Paths.get(gauge_project_root, "resources", "LocalTemplates", language);
        if (!Files.exists(templatePath)) {
            return false;
        }
        try {
            FileUtils.copyDirectory(templatePath.toFile(), this.projectDir);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getStdOut() {
        return lastProcessStdout;
    }

    public File getProjectDir() {
        return projectDir;
    }

    public Specification createSpecification(String specsDirName, String name) throws IOException {
        String specsDir = StringUtils.isEmpty(specsDirName) ? GaugeProject.specsDirName : specsDirName;
        File specFile = getSpecFile(name, specsDir);
        if (specFile.exists()) {
            throw new RuntimeException("Failed to create specification with name: " + name + "." + specFile.getAbsolutePath() + ": File already exists");
        }
        Specification specification = new Specification(name);
        specification.saveAs(specFile);
        specifications.add(specification);
        return specification;
    }

    private File getSpecFile(String name, String dirPath) {
        name = Util.getSpecName(name);
        return getFile(name, dirPath, ".spec");
    }

    private File getFile(String name, String dirPath, String extension) {
        if (!new File(projectDir, dirPath).exists()) {
            new File(projectDir, dirPath).mkdirs();
        }
        return new File(projectDir, Util.combinePath(dirPath, name) + extension);
    }

    private File getSpecFile(String name) {
        return getSpecFile(name, "");
    }

    public Specification findSpecification(String specName) {
        for (Specification specification : specifications) {
            if (specification.getName().equalsIgnoreCase(specName)) {
                return specification;
            }
        }

        return null;
    }

    public Scenario findScenario(String scenarioName, List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            if (scenario.getName().equalsIgnoreCase(scenarioName)) {
                return scenario;
            }
        }
        return null;
    }
    
    public boolean executeSpecFolder(String specFolder) throws Exception {
        return executeGaugeCommand(new String[]{"run", "--simple-console", "--verbose", specFolder}, null);
    }

    public boolean executeSpecFromFolder(String spec, String specFolder) throws Exception {
        File oldProjectDir = this.projectDir;
        this.projectDir = new File(oldProjectDir, specFolder);
        boolean exitCode = executeGaugeCommand(new String[]{"run", "--simple-console", "--verbose", spec}, null);
        this.projectDir = oldProjectDir;
        return exitCode;
    }

    public ExecutionSummary generateSpectacleDocumentation() throws Exception {
        String[] args = new String[]{"docs", "spectacle", "specs/"};
        boolean success = executeGaugeCommand(args, null);
        return new ExecutionSummary(String.join(" ", args), success, lastProcessStdout, lastProcessStderr);
    }

    private boolean executeGaugeCommand(String[] args, HashMap<String, String> envVars) throws IOException, InterruptedException {
        ArrayList<String> command = new ArrayList<>();
        command.add(executableName);
        Collections.addAll(command, args);
        ProcessBuilder processBuilder = new ProcessBuilder(command.toArray(new String[command.size()]));
        processBuilder.directory(projectDir);
        String gauge_project_root = System.getenv("GAUGE_PROJECT_ROOT");
        String folderName = (String) ScenarioDataStore.get("log_proj_name");
        String logFolder = Util.combinePath(new File("./testLogs").getAbsolutePath(), folderName);
        String localNugetPath = Paths.get(gauge_project_root, "resources", "LocalNuget").toAbsolutePath().toString();

        filterParentProcessGaugeEnvs(processBuilder);
        filterConflictingEnv(processBuilder);

        processBuilder.environment().put("NUGET_ENDPOINT", localNugetPath);
        processBuilder.environment().put("screenshot_on_failure", "true");
        processBuilder.environment().put("GAUGE_TELEMETRY_ENABLED", "false");
        processBuilder.environment().put("PYTHONUNBUFFERED", "1");
        processBuilder.environment().put("logs_directory", logFolder);
        if(Util.getCurrentLanguage().equals("java")) processBuilder.environment().put("enable_multithreading", "true");

        if (envVars != null) {
            processBuilder.environment().putAll(envVars);
        }

        Process lastProcess = processBuilder.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(lastProcess.getInputStream()));
        String line;
        String newLine = System.getProperty("line.separator");
        lastProcessStdout = "";
        while ((line = br.readLine()) != null) {
            lastProcessStdout = lastProcessStdout.concat(line).concat(newLine);
        }
        lastProcessStderr = "";
        br = new BufferedReader(new InputStreamReader(lastProcess.getErrorStream()));
        while ((line = br.readLine()) != null) {
            lastProcessStderr = lastProcessStderr.concat(line).concat(newLine);
        }
        lastProcess.waitFor();
        return lastProcess.exitValue() == 0;
    }

    public void deleteSpec(String specName) {
        getSpecFile(specName).delete();
    }

    private void filterConflictingEnv(ProcessBuilder processBuilder) {
        processBuilder.environment().keySet().stream()
                .filter(env -> !PRODUCT_ENVS.contains(env.toUpperCase()) && env.toUpperCase().contains(PRODUCT_PREFIX))
                .forEach(env -> processBuilder.environment().put(env, ""));
    }

    private void filterParentProcessGaugeEnvs(ProcessBuilder processBuilder) {
        GAUGE_ENVS.stream().forEach(env -> processBuilder.environment().remove(env));
    }

    public static void implement(Table impl, TableRow row, boolean appendCode) throws Exception {
        if (impl.getColumnNames().contains("implementation")) {
            StepImpl stepImpl = new StepImpl(row.getCell("step text"), row.getCell("implementation"),
                    Boolean.parseBoolean(row.getCell("continue on failure")), appendCode,
                    row.getCell("error type"), row.getCell("implementation dir"));
            if(impl.getColumnNames().contains("package_name"))
                stepImpl.setPackageName(row.getCell("package_name"));
            getCurrentProject().implementStep(stepImpl);
        }
    }

    public abstract void implementStep(StepImpl stepImpl) throws Exception;

    public abstract Map<String, String> getLanguageSpecificFiles();

    public abstract List<String> getLanguageSpecificGitIgnoreText();

    public abstract String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode);

    public abstract void createHookWithPrint(String hookLevel, String hookType, String implementation) throws Exception;

    public abstract void createHookWithException(String hookLevel, String hookType) throws IOException;

    public abstract void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException;

    public String getLastProcessStdout() {
        return lastProcessStdout;
    }

    public String getLastProcessStderr() {
        return lastProcessStderr;
    }

    public ArrayList<Specification> getAllSpecifications() {
        return specifications;
    }

    public abstract String getDataStoreWriteStatement(TableRow row, List<String> columnNames);

    public abstract String getDataStorePrintValueStatement(TableRow row, List<String> columnNames);

    public abstract void configureCustomScreengrabber(String screenshotFile) throws IOException;

}
