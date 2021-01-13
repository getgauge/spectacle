package com.thoughtworks.gauge.test.common;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.test.StepImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotnetProject extends GaugeProject {
    private static final String DEFAULT_AGGREGATION = "And";

    public DotnetProject(String projName) throws IOException {
        super("dotnet", projName);
    }

    public Map<String, String> getLanguageSpecificFiles() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("StepImplementation.cs", "file");
        return map;
    }

    @Override
    public List<String> getLanguageSpecificGitIgnoreText() {
        return new ArrayList<>();
    }

    private StringBuilder createStepTeplate(ArrayList<String> stepTexts) {
        StringBuilder step = new StringBuilder();
        if(stepTexts.size()==1){
            return step.append("[Step(\"").append(stepTexts.get(0)).append("\")]\n");
        }
        else {
            StringBuilder commaSeparated = new StringBuilder();
            for(String stepText:stepTexts){
                commaSeparated.append("\"").append(stepText).append("\",");
            }
            StringBuilder res = step.append("[Step(").append(commaSeparated.substring(0,commaSeparated.length()-1)).append(")]\n");
            return res;
        }
    }

    @Override
    public void implementStep(StepImpl stepImpl) throws Exception {
        List<String> paramTypes = new ArrayList<String>();
        StepValueExtractor stepValueExtractor = new StepValueExtractor();
        StepValueExtractor.StepValue stepValue =  stepValueExtractor.getFor(stepImpl.getFirstStepText());
        String className = Util.getUniqueName();
        StringBuilder classText = new StringBuilder();
        classText.append("public class ").append(className).append("\n{\n");
        classText.append(createStepTeplate(stepValueExtractor.getValueFor(stepImpl.getAllStepTexts())));
        if (stepImpl.isContinueOnFailure()) {
            classText.append("[ContinueOnFailure]\n");
        }
        classText.append("public void ").append("stepImplementation(");
        for (int i = 0; i < stepValue.paramCount; i++) {
            if (i + 1 == stepValue.paramCount) {
                classText.append("object param").append(i);
            } else {
                classText.append("object param").append(i).append(", ");
            }
            paramTypes.add("Object");
        }
        String implementation = stepImpl.getImplementation();
        implementation = getStepImplementation(stepValue, implementation, paramTypes, stepImpl.isValidStatement());
        classText.append(")\n{\n").append(implementation).append("\n}\n");
        classText.append("}\n");
        Util.appendToFile(Util.combinePath(getStepImplementationsDir(), "StepImplementation.cs"), classText.toString());
    }

    @Override
    public String getStepImplementation(StepValueExtractor.StepValue stepValue, String implementation, List<String> paramTypes, boolean appendCode) {
        StringBuilder builder = new StringBuilder();
        if (implementation.equalsIgnoreCase(PRINT_PARAMS)) {
            builder.append("Console.WriteLine(");
            for (int i = 0; i < stepValue.paramCount; i++) {
                builder.append("\"param").append(i).append("=\"+").append("param").append(i);
                if (i != stepValue.paramCount - 1) {
                    builder.append("+\",\"+");
                }
            }
            builder.append(");\n");
        } else if (implementation.equalsIgnoreCase(THROW_EXCEPTION)) {
            return "throw new SystemException();";
        } else if (implementation.equalsIgnoreCase(CAPTURE_SCREENSHOT)) {
            return "Gauge.CSharp.Lib.GaugeScreenshots.Capture();";
        } else {
            if (appendCode) {
                builder.append(implementation);
            } else {
                builder.append("Console.WriteLine(").append(implementation).append(");\n");
            }
        }
        return builder.toString();
    }

    @Override
    public void createHookWithPrint(String hookLevel, String hookType, String printStatement) throws Exception {
        String implementation = String.format("Console.WriteLine(\"%s\");", printStatement);
        String method = createHookMethod(hookLevel, hookType, implementation);
        createHook(method);
    }

    @Override
    public void createHookWithException(String hookLevel, String hookType) throws IOException {
        String method = createHookMethod(hookLevel, hookType, "throw new SystemException();");
        createHook(method);
    }

    @Override
    public void createHooksWithTagsAndPrintMessage(String hookLevel, String hookType, String printString, String aggregation, Table tags) throws IOException {
        String implementation = String.format("Console.WriteLine(\"%s\");", printString);
        String method = createHookMethod(hookLevel, hookType, implementation, tags.getColumnValues("tags"), aggregation);
        createHook(method);
    }


    private String createHookMethod(String hookLevel, String hookType, String implementation) {
        return createHookMethod(hookLevel, hookType, implementation, new ArrayList<String>(), DEFAULT_AGGREGATION);
    }

    private String createHookMethod(String hookLevel, String hookType, String implementation, List<String> tags, String aggregation) {
        StringBuilder methodText = new StringBuilder();
        String hookString = hookString(hookLevel, hookType, tags);
        methodText.append(hookString).append("\n");
        methodText.append(aggregationAttribute(aggregation));
        methodText.append(String.format("public void %s() {\n", Util.getUniqueName()));
        methodText.append(String.format("%s\n", implementation));
        methodText.append("\n}\n");
        return methodText.toString();
    }

    private String aggregationAttribute(String aggregation) {
        if (aggregation.equals("AND") || aggregation.equals(DEFAULT_AGGREGATION)) {
            return String.format("[TagAggregationBehaviour(TagAggregation.%s)]\n", DEFAULT_AGGREGATION);
        }
        return String.format("[TagAggregationBehaviour(TagAggregation.%s)]\n", "Or");
    }

    private void createHook(String method) throws IOException {
        StringBuilder classText = new StringBuilder();
        String className = Util.getUniqueName();
        classText.append("public class ").append(className).append("{\n");
        classText.append(method);
        classText.append("\n}\n");
        Util.appendToFile(Util.combinePath(getStepImplementationsDir(), "StepImplementation.cs"), classText.toString());
    }

    @Override
    public String getDataStoreWriteStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        String value = row.getCell("value");
        return "DataStoreFactory." + dataStoreType + "DataStore.Add(\"" + key + "\",\"" + value + "\");";
    }

    @Override
    public String getDataStorePrintValueStatement(TableRow row, List<String> columnNames) {
        String dataStoreType = row.getCell("datastore type");
        String key = row.getCell("key");
        return "Console.WriteLine(DataStoreFactory." + dataStoreType + "DataStore.Get(\"" + key + "\"));";
    }

    @Override
    public void configureCustomScreengrabber(String screenshotFile) throws IOException {
        String className = Util.getUniqueName();
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("public class " + className + " : ICustomScreenshotWriter {\n");
        sb.append("\n");
        sb.append("    public string TakeScreenShot() {\n");
        sb.append("        return \"" + screenshotFile + "\";\n");
        sb.append("    }\n");
        sb.append("}");
        Util.appendToFile(Util.combinePath(getStepImplementationsDir(), "StepImplementation.cs"), sb.toString());

    }

    private String hookString(String hookLevel, String hookType, List<String> tags) {
        String tagsText = isSuiteLevel(hookLevel) ? "" : Util.joinList(Util.quotifyValues(tags));
        return String.format("[%s(%s)]", hookName(hookLevel, hookType), tagsText);
    }

    private String hookName(String hookLevel, String hookType) {
        return String.format("%s%s", Util.capitalize(hookType), Util.capitalize(hookLevel));
    }

    private boolean isSuiteLevel(String hookLevel) {
        return hookLevel.trim().equals("suite");
    }

    private String getStepImplementationsDir() {
        return getProjectDir().getAbsolutePath();
    }
}
