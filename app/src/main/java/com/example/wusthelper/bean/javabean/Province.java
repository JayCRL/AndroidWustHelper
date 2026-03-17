package com.example.wusthelper.bean.javabean;

import org.jaaksi.pickerview.dataset.OptionDataSet;

import java.util.List;

public class Province implements OptionDataSet {
    public int id;
    public String name;
    public List<City> citys;

    @Override
    public CharSequence getCharSequence() {
        return name;
    }

    @Override
    public List<City> getSubs() {
        return citys;
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

    public void setCitys(List<City> citys) {
        this.citys = citys;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<City> getCitys() {
        return citys;
    }
}
