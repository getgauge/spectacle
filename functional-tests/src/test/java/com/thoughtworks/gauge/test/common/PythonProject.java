package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.StepImpl;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonProject extends GaugeProject {
    private static final String DEFAULT_AGGREGATION = "AND";
    public static final String IMPORT = "from getgauge.python import step, after_step, before_step, after_scenario, before_scenario, after_spec, before_spec, after_suite, before_suite, Messages, DataStoreFactory, continue_on_failure, Screenshots\n";

    public PythonProject(String projName) throws IOException {
        super("python", projName);
    }

    private StringBuilder createStepTeplate(ArrayList<String> stepTexts) {
        StringBuilder step = new StringBuilder();
        if(stepTexts.size()==1){
            return step.append("@step(\"").append(stepTexts.get(0)).append("\")\n");
        }
        else {
            StringBuilder commaSeparated = new StringBuilder();
            for(String stepText:stepTexts){
                commaSeparated.append("\"").append(stepText).append("\",");
            }
            return step.append("@step([").append(commaSeparated).append("])\n");
        }
    }

    @Override
    public void implementStep(StepImpl stepImpl) throws Exception {
        List<String> paramTypes = new ArrayList<String>();
        StepValueExtractor stepValueExtractor = new StepValueExtractor();
        ArrayList<String> stepValues = stepValueExtractor.getValueFor(stepImpl.getAllStepTexts());
        StepValueExtractor.StepValue stepValue = stepValueExtractor.getFor(stepImpl.getFirstStepText());
        String fileName = Util.getUniqueName();
        StringBuilder classText = new StringBuilder();
        classText.append(IMPORT);
        if (stepImpl.isContinueOnFailure())
            classText.append("@continue_on_failure([Exception])\n");
        classText.append(createStepTeplate(stepValues));
        classText.append("def ").append(Util.getUniqueName()).append("(");
        for (int i = 0; i < stepValue.paramCount; i++) {
            if (i + 1 == stepValue.paramCount) {
                classText.append("param").append(i);
            } else {
                classText.append("param").append(i).append(", ");
            }
            paramTypes.add("string");
        }
        String implementation = stepImpl.getImplementation();
        implementation = getStepImplementation(stepValue, implementation, paramTypes, stepImpl.isValidStatement());
        classText.append("):\n").append(implementation).append("\n\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), fileName + ".py"), classText.toString());
    }

    @Override
    public Map<String, String> getLanguageSpecificFiles() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("step_impl", "dir");
        map.put(Util.combinePath("step_impl", "step_impl.py"), "file");
        return map;
    }

    @Override
    public List<String> getLanguageSpecificGitIgnoreText() {
        return new ArrayList<String>() {{
            add("# Gauge - metadata dir\n.gauge");
            add("# Gauge - log files dir\nlogs");
            add("# Gauge - reports dir\nreports");
            add("# Gauge - python compiled files\n*.pyc");
        }};
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        StringBuilder builder = new StringBuilder();
        if (implementation.equalsIgnoreCase(PRINT_PARAMS)) {
            builder.append("    print(");
            for (int i = 0; i < stepValue.paramCount; i++) {
                if (paramTypes.get(i).toLowerCase().equals("string")) {
                    builder.append("\"param").append(i).append("=\"+").append("param").append(i).append(".__str__()");
                    if (i != stepValue.paramCount - 1) {
                        builder.append("+\",\"+");
                    }
                }
            }
            builder.append(")\n");
        } else if (implementation.equalsIgnoreCase(THROW_EXCEPTION)) {
            builder.append("    raise Exception('I do not know Python!')\n");
        } else if (implementation.equalsIgnoreCase(CAPTURE_SCREENSHOT)) {
            builder.append("    Screenshots.capture_screenshot()");
        } else {
            if (appendCode) {
                builder.append(implementation);
            } else {
                implementation = implementation.isEmpty() ? "" : implementation + ".__str__()";
                builder.append("    print(").append(implementation).append(")\n");
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
        StringBuilder fileText = new StringBuilder();
        fileText.append(String.format(IMPORT + "@%s_%s\ndef %s():    raise Exception('I do not know Python!')\n", hookType, hookLevel, Util.getUniqueName()));
        fileText.append("\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".py"), fileText.toString());
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException {
        createHook(hookLevel, hookType, printString, aggregation, tags.getColumnValues("tags"));
    }

    public void createHook(String hookLevel, String hookType, String printString, String aggregation, List<String> tags) throws IOException {
        StringBuilder fileText = new StringBuilder();
        if (!isSuiteHook(hookLevel))
            fileText.append(String.format(IMPORT + "@%s_%s%s\ndef %s():\n    print(\"%s \")\n", hookType, hookLevel, getOptions(aggregation, tags), Util.getUniqueName(), printString));
        else
            fileText.append(String.format(IMPORT + "@%s_%s\ndef %s():\n    print(\"%s \")\n", hookType, hookLevel, Util.getUniqueName(), printString));
        fileText.append("\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".py"), fileText.toString());
    }

    @Override
    public String getDataStoreWriteStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        String value = row.getCell("value");
        return "    print(DataStoreFactory." + dataStoreType.toLowerCase() + "_data_store().put(\"" + key + "\", \"" + value + "\"))";
    }

    private String getOptions(String aggregation, List<String> tags) {
        return tags.size() < 1
                ? ""
                : "('<" + StringUtils.join(tags, "> " + aggregation.toLowerCase() + " <") + ">')";
    }

    @Override
    public String getDataStorePrintValueStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        return "    print(DataStoreFactory." + dataStoreType.toLowerCase() + "_data_store().get(\"" + key + "\"))";
    }

    @Override
    public void configureCustomScreengrabber(String stubScreenshot) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("from getgauge.python import custom_screenshot_writer\nimport os");
        sb.append("\n");
        sb.append("@custom_screenshot_writer\n");
        sb.append("def takeScreenshot():\n");
        sb.append("    return \"").append(stubScreenshot).append("\"\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".py"), sb.toString());
    }

    private String getStepImplementationsDir() {
        return new File(getProjectDir(), "step_impl").getAbsolutePath();
    }

    private boolean isSuiteHook(String hookLevel) {
        return hookLevel.trim().equals("suite");
    }
}
