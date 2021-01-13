package com.thoughtworks.gauge.test.common;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

    private String name;
    List<Item> items = new ArrayList<>();
    List<String> tags = new ArrayList<String>();

    public Scenario(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(String item, String type) {
        if (type.equalsIgnoreCase(Comment.TYPE)) {
            this.items.add(new Comment(item));
            return;
        }
        this.items.add(new Step(item));
    }

    public void clearItems() {
        this.items.clear();
    }

    public void addTags(String newTags) {
        tags.add(newTags);
    }

}
