package com.example.wusthelper.bean.javabean;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

public class LibraryHistoryBean {

    public static final int TITLE = 0;

    public static final int CONTENT = 1;

    public static final int BEYOND_TIME = 2;

    public static final int BORROWING = 3;

    public static final int HISTORY_BOOK = 4;

    private int type;

    private int title;

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    private Drawable titleSign;

    public Drawable getTitleSign() {
        return titleSign;
    }

    public void setTitleSign(Drawable titleSign) {
        this.titleSign = titleSign;
    }

    private String titleName;

    private int num;

    @SerializedName("bookCode")
    private String callNumber;

    @SerializedName("bookName")
    private String author;

//    @SerializedName("bookName")
//    private String bookName;

    @SerializedName("rentTime")
    private String borrowDate;

    @SerializedName("returnTime")
    private String returnDate;

//    public String getBookName() {
//        return bookName;
//    }
//
//    public void setBookName(String bookName) {
//        this.bookName = bookName;
//    }

    @SerializedName("bookPlace")
    private String returnPlace;

    private String function;

    @SerializedName("bookUrl")
    private String href;


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public LibraryHistoryBean() {
    }

    public LibraryHistoryBean(int title, int type, Drawable titleSign, String titleName, int num) {
        this.title = title;
        this.titleSign = titleSign;
        this.type = type;
        this.titleName = titleName;
        this.num = num;
    }

    public LibraryHistoryBean(int title, int type, String callNumber, String author, String borrowDate, String returnDate, String returnPlace, String function, String href) {
        this.title = title;
        this.type = type;
        this.callNumber = callNumber;
        this.author = author;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.returnPlace = returnPlace;
        this.function = function;
        this.href = href;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getReturnPlace() {
        return returnPlace;
    }

    public void setReturnPlace(String returnPlace) {
        this.returnPlace = returnPlace;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

}
