package com.example.wusthelper.bean.javabean;

import org.jaaksi.pickerview.dataset.OptionDataSet;

import java.util.List;

public class City implements OptionDataSet {

    public int id;
    public String name;
    public List<County> counties;

    @Override
    public CharSequence getCharSequence() {
        return name;
    }

    @Override
    public List<County> getSubs() {
        return counties;
    }

    @Override
    public String getValue() {
        return String.valueOf(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCounties(List<County> counties) {
        this.counties = counties;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<County> getCounties() {
        return counties;
    }
}
