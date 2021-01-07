package com.thoughtworks.gauge.test;

import java.util.ArrayList;
import java.util.List;

public class StepImpl {

    private String stepText;
    private String implementation;
    private boolean continueOnFailure;
    private boolean isValidStatement;
    private final String implementationDir;
    private List<String> errorTypes;
    private String packageName;

    public StepImpl(String stepText, String implementation, boolean continueOnFailure, boolean isValidStatement, String errorTypes, String implementationDir) {
        this.stepText = stepText;
        this.implementation = implementation;
        this.continueOnFailure = continueOnFailure;
        this.isValidStatement = isValidStatement;
        this.implementationDir = implementationDir;
        this.errorTypes = new ArrayList<String>();
        this.errorTypes.add(errorTypes);
    }

    public String[] getAllStepTexts(){
        if(!stepText.startsWith("["))
            return new String[]{stepText};

        return stepText.substring(1, stepText.length() - 1).split(",");
    }

    public String getFirstStepText(){
        return getAllStepTexts()[0];
    }

    public String getImplementation() {
        return implementation;
    }

    public boolean isContinueOnFailure() {
        return continueOnFailure;
    }

    public boolean isValidStatement() {
        return isValidStatement;
    }

    public List<String> getErrorTypes() {
        return errorTypes;
    }

    public String getImplementationDir() {
        return implementationDir;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
