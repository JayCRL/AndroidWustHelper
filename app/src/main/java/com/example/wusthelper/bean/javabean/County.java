package com.example.wusthelper.bean.javabean;

import org.jaaksi.pickerview.dataset.OptionDataSet;

import java.util.List;

public class County implements OptionDataSet {
    public int id;
    public String name;

    @Override
    public CharSequence getCharSequence() {
        return name;
    }

    @Override
    public List<OptionDataSet> getSubs() {
        return null;
    }

    @Override
    public String getValue() {
        return String.valueOf(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
