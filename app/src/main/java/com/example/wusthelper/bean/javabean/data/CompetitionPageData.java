package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CompetitionPageData<T> {
    public int total;
    public List<T> records = new ArrayList<>();
}
