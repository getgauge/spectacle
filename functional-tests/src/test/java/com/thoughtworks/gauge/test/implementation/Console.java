package com.thoughtworks.gauge.test.implementation;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import com.thoughtworks.gauge.test.common.Util;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;
import static org.assertj.core.api.Assertions.assertThat;

public class Console {

    @Step("Console should not contain following lines <table>")
    public void consoleShouldNotContainFollowingLines(Table table) throws IOException {
        String output = getCurrentProject().getStdOut();
        for (String s : table.getColumnValues(0)) {
            assertThat(output).doesNotContain(s);
        }
    }

    @Step("Console should contain <text> <number of times> times")
    public void consoleShouldContain(String text, int numberOfTimes) throws IOException {
        String output = getCurrentProject().getStdOut();
        int matchCount = Util.countOccurrences(output, text);
        String errorMessage = "Expected '" + output + "' to have '" + text + "' " + numberOfTimes + " times. Found " + matchCount + " times.";

        assertThat(matchCount).withFailMessage(errorMessage).isEqualTo(numberOfTimes);
    }

    @Step({"Console should contain <message>", "The error message <message> should be displayed on console"})
    public void consoleShouldContain(String message) throws IOException {
        String output = getCurrentProject().getStdOut();
        assertThat(output).contains(message);
    }

    @Step("Console should contain following lines in order <console output table>")
    public void consoleShouldContainFollowingLinesInOrder(Table table) throws IOException {
        String output = getCurrentProject().getStdOut();

        for (String s : table.getColumnValues(0)) {
            assertThat(output).contains(s);
        }
    }

    @Step("Console should contain <duplicateConceptNumbers> duplicate concept definition list <conceptName>")
    @SuppressWarnings("unchecked")
    public void consoleShouldContainDuplicateConceptDefinitionList(int duplicateConceptNumbers, String conceptName) throws IOException {
        String output = getCurrentProject().getStdOut();

        assertThat(output).contains("[ParseError]");
        assertThat(output).contains("Duplicate concept definition found => '"+conceptName+"'");

        List<String> names = (List<String>) ScenarioDataStore.get(conceptName);
        assertThat(names).asList().hasSize(duplicateConceptNumbers);

        for (String s : names) {
            assertThat(output).contains(s + ":1");
        }
    }

    @Step("Console should contain following output for <count> times <table>")
    public void conatinsInformationOnConsoleForTimes(int count, Table consoleoutPut) throws IOException {
        String output = getCurrentProject().getStdOut();
        for (String s : consoleoutPut.getColumnValues(0)) {
            int actualCount = StringUtils.countMatches(output, s);
            assertThat(actualCount).isEqualTo(count);
        }
    }
}
