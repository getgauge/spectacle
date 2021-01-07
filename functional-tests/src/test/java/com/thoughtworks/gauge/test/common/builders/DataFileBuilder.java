package com.thoughtworks.gauge.test.common.builders;

import com.thoughtworks.gauge.test.common.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;

public class DataFileBuilder {
    private String csvFile;
    private String txtFile;
    private String subDirPath;
    private List<String> content;

    public void build() throws IOException {
        File file = null;
        if (csvFile != null)
            file = getCurrentProject().createCsv(csvFile, subDirPath);

        if (txtFile != null)
            file = getCurrentProject().createTxt(txtFile, subDirPath);

        Util.writeToFile(file.getAbsolutePath(), (content == null || content.isEmpty()) ? this.toString() : String.join("\n", content));
    }

    public DataFileBuilder withSubDirPath(String subDirPath) {
        this.subDirPath = subDirPath;
        return this;
    }

    public DataFileBuilder withCsvFile(String csvFile) {
        this.csvFile = csvFile;
        return this;
    }

    public DataFileBuilder withTextFile(String txtFile) {
        this.txtFile = txtFile;
        return this;
    }

    public DataFileBuilder withContent(List<String> content) {
        this.content = content;
        return this;
    }
}
