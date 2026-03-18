package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class PageCommodity {
    public List<Commodity> records = new ArrayList<>();
    public int total;
    public int size;
    public int current;
    public int pages;
}
