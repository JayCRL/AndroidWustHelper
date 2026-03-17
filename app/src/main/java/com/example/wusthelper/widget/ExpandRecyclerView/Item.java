package com.example.wusthelper.widget.ExpandRecyclerView;

public class Item {

    private final String name;
    private final int id;

    public Item(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
