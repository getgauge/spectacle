package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.StepImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JavascriptProject extends GaugeProject {
    private static final String DEFAULT_AGGREGATION = "AND";

    public JavascriptProject(String projName) throws IOException {
        super("js", projName);
    }

    public static String toTitleCase(String input) {
        input = input.toLowerCase();
        char c = input.charAt(0);
        String s = "" + c;
        String f = s.toUpperCase();
        return f + input.substring(1);
    }

    private StringBuilder createStepTeplate(ArrayList<String> stepTexts) {
        StringBuilder step = new StringBuilder();
        if (stepTexts.size() == 1) {
            return step.append("step(\"").append(stepTexts.get(0)).append("\",");
        } else {
            StringBuilder commaSeparated = new StringBuilder();
            for (int i = 0; i < stepTexts.size(); i++) {
                commaSeparated.append("\"").append(stepTexts.get(i)).append("\"");
                if (i != stepTexts.size() - 1) commaSeparated.append(",");
            }
            return step.append("step([").append(commaSeparated).append("],");
        }
    }

    @Override
    public void implementStep(StepImpl stepImpl) throws Exception {
        List<String> paramTypes = new ArrayList<String>();
        StepValueExtractor stepValueExtractor = new StepValueExtractor();
        StepValueExtractor.StepValue stepValue = stepValueExtractor.getFor(stepImpl.getFirstStepText());
        String fileName = Util.getUniqueName();
        StringBuilder jsCode = new StringBuilder(createStepTeplate(stepValueExtractor.getValueFor(stepImpl.getAllStepTexts())));
        if (stepImpl.isContinueOnFailure()) {
            jsCode.append(" { continueOnFailure: true },");
        }
        jsCode.append(" function (");
        for (int i = 0; i < stepValue.paramCount; i++) {
            jsCode.append("param").append(i).append(", ");
            paramTypes.add("string");
        }
        jsCode.append("done) {\n");
        jsCode.append(getStepImplementation(stepValue, stepImpl.getImplementation(), paramTypes, stepImpl.isValidStatement()));
        jsCode.append("\ndone();");
        jsCode.append("\n});");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), fileName + ".js"), jsCode.toString());
    }

    @Override
    public Map<String, String> getLanguageSpecificFiles() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("tests", "dir");
        map.put(Util.combinePath("tests", "step_implementation.js"), "file");
        map.put(Util.combinePath("env", "default", "js.properties"), "file");
        return map;
    }

    @Override
    public List<String> getLanguageSpecificGitIgnoreText() {
        return new ArrayList<String>() {{
            add("# Gauge - metadata dir\n.gauge");
            add("# Gauge - log files dir\nlogs");
            add("# Gauge - reports dir\nreports");
            add("# Gauge - JavaScript node dependencies\nnode_modules");
        }};
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        StringBuilder builder = new StringBuilder();
        if (implementation.equalsIgnoreCase(PRINT_PARAMS)) {
            builder.append("  console.log(");
            for (int i = 0; i < stepValue.paramCount; i++) {
                if (paramTypes.get(i).toLowerCase().equals("string")) {
                    builder.append(String.format("\"param%d=\"+param%d", i, i));
                    if (i != stepValue.paramCount - 1) {
                        builder.append("+\",\"+");
                    }
                }
            }
            builder.append(");\n");
        } else if (implementation.toLowerCase().startsWith("throw")) {
            builder.append("  throw new Error(\"exception raised\");\n\n");
        } else if (implementation.equalsIgnoreCase(CAPTURE_SCREENSHOT)){
            builder.append("gauge.screenshot();\n\n");
        } else {
            if (appendCode) {
                builder.append(implementation);
            } else {
                builder.append("  console.log(").append(implementation).append(");\n");
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
        StringBuilder jsFileText = new StringBuilder();
        jsFileText.append(String.format("%s%s(", hookType.toLowerCase(), toTitleCase(hookLevel)));
        jsFileText.append("function () {\nthrow new Error(\"exception was raised\");\n}");
        jsFileText.append(");\n");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".js"), jsFileText.toString());
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException {
        createHook(hookLevel, hookType, printString, aggregation, tags.getColumnValues("tags"));
    }

    public void createHook(String hookLevel, String hookType, String printString, String aggregation, List<String> tags) throws IOException {
        StringBuilder jsFileText = new StringBuilder();
        jsFileText.append(String.format("%s%s(function () {", hookType.toLowerCase(), toTitleCase(hookLevel)));
        jsFileText.append(String.format("  console.log(\"%s\");\n", printString));
        jsFileText.append(String.format("}, %s);\n", getOptions(aggregation, tags)));
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), Util.getUniqueName() + ".js"), jsFileText.toString());
    }

    @Override
    public String getDataStoreWriteStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        String value = row.getCell("value");
        return "gauge.dataStore." + dataStoreType.toLowerCase() + "Store.put(\"" + key + "\", \"" + value + "\");";
    }

    private String getOptions(String aggregation, List<String> tags) {
        String tagsText = Util.joinList(Util.quotifyValues(tags));
        return String.format("{tags: [%s], operator: \"%s\"}", tagsText, aggregation);
    }

    @Override
    public String getDataStorePrintValueStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        return "console.log(gauge.dataStore." + dataStoreType.toLowerCase() + "Store.get(\"" + key + "\"));";
    }

    @Override
    public void configureCustomScreengrabber(String stubScreenshotFile) throws IOException {
        String className = Util.getUniqueName();
        StringBuilder sb = new StringBuilder();
        sb.append("var fs = require('fs');\n");
        sb.append("\n");
        sb.append("gauge.customScreenshotWriter = async function () {\n");
        sb.append("\n");
        sb.append("    return \""+stubScreenshotFile+"\";\n");
        sb.append("};");
        Util.writeToFile(Util.combinePath(getStepImplementationsDir(), className + ".js"), sb.toString());
    }

    private String getStepImplementationsDir() {
        return new File(getProjectDir(), "tests").getAbsolutePath();
    }
}
