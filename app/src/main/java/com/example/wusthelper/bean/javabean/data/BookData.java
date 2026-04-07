package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.BookDetailBean;
import com.google.gson.annotations.SerializedName;

public class BookData extends BaseData{

    @SerializedName("data")
    private BookDetailBean data;

    public BookDetailBean getData() {
        return data;
    }

    public void setData(BookDetailBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BookData{" +
                "data=" + data +
                '}';
    }
}
