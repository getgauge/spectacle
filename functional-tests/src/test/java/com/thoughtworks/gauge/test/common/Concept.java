package com.thoughtworks.gauge.test.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Concept {

    private String name;
    private File conceptFile = null;
    private List<Item> items;

    public Concept(String name) {
        this.name = name;
        items = new ArrayList<>();
    }

    public void addItem(String item, String type) {
        if (type.equalsIgnoreCase(Comment.TYPE)) {
            this.items.add(new Comment(item));
            return;
        }
        this.items.add(new Step(item));
    }

    @Override
    public String toString() {
        StringBuilder conceptText = new StringBuilder();
        conceptText.append("# ").append(name).append("\n");
        for (Item item : items) {
            conceptText.append(item.toString()).append("\n");
        }
        conceptText.append("\n");
        return conceptText.toString();
    }

    public void saveAs(File file) throws IOException {
        Util.writeToFile(file.getAbsolutePath(), this.toString());
        this.conceptFile = file;
    }

    public File getConceptFile() {
        return conceptFile;
    }
}
