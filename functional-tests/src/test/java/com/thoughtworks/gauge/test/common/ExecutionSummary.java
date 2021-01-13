package com.thoughtworks.gauge.test.common;

public class ExecutionSummary {
    private boolean Success;
    private String command;
    private String stdout;
    private String stderr;


    public ExecutionSummary(String command, boolean success, String stdout, String stderr) {
        this.command = command;
        this.stdout = stdout;
        this.Success = success;
        this.stderr = stderr;
    }

    public boolean getSuccess() {
        return Success;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public String getCommand() {
        return command;
    }
}
