package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class CollectionBookBean {

    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String bookName;
    @SerializedName("author")
    private String author;

    @SerializedName("publisher")
    private String press;

    @SerializedName("callNo")
    private String callNumber;

    @SerializedName("detailUrl")
    private String url;

    public CollectionBookBean() {
    }

    public CollectionBookBean(String bookName, String author, String press, String callNumber, String url) {
        this.bookName = bookName;
        this.author = author;
        this.press = press;
        this.callNumber = callNumber;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

}
