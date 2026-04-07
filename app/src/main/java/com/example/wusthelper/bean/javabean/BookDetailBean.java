package com.example.wusthelper.bean.javabean;

import com.example.wusthelper.bean.javabean.LibraryCollectionBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BookDetailBean {

    @SerializedName("bookNameAndAuth")
    private String bookNameAndAuth;
    @SerializedName("publisher")
    private String publisher;
    @SerializedName("introduction")
    private String introduction;
    @SerializedName("imgUrl")
    private String imgUrl;
    @SerializedName("isbn")
    private String isbn;
    @SerializedName("isCollection")
    private String isCollection;
    @SerializedName("list")
    private List<LibraryCollectionBean> libraryCollectionBeanList = new ArrayList<>();

    public String getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(String isCollection) {
        this.isCollection = isCollection;
    }

    public String getBookNameAndAuth() {
        return bookNameAndAuth;
    }

    public void setBookNameAndAuth(String bookNameAndAuth) {
        this.bookNameAndAuth = bookNameAndAuth;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<LibraryCollectionBean> getLibraryCollectionBeanList() {
        return libraryCollectionBeanList;
    }

    public void setLibraryCollectionBeanList(List<LibraryCollectionBean> libraryCollectionBeanList) {
        this.libraryCollectionBeanList = libraryCollectionBeanList;
    }

    @Override
    public String toString() {
        return "BookDetailBean{" +
                "bookNameAndAuth='" + bookNameAndAuth + '\'' +
                ", publisher='" + publisher + '\'' +
                ", introduction='" + introduction + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", isbn='" + isbn + '\'' +
                ", isCollection='" + isCollection + '\'' +
                ", libraryCollectionBeanList=" + libraryCollectionBeanList +
                '}';
    }
}
