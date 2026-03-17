package com.example.wusthelper.bean.javabean;

public class LibraryNotificationBean {

    private String libraryNotificationName;

    private String libraryNotificationDate;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LibraryNotificationBean() {
    }

    public LibraryNotificationBean(String libraryNotificationName, String libraryNotificationDate, String url) {
        this.libraryNotificationName = libraryNotificationName;
        this.libraryNotificationDate = libraryNotificationDate;
        this.url = url;
    }

    public LibraryNotificationBean(String libraryNotificationName, String libraryNotificationDate) {
        this.libraryNotificationName = libraryNotificationName;
        this.libraryNotificationDate = libraryNotificationDate;
    }

    public String getLibraryNotificationName() {
        return libraryNotificationName;
    }

    public void setLibraryNotificationName(String libraryNotificationName) {
        this.libraryNotificationName = libraryNotificationName;
    }

    public String getLibraryNotificationDate() {
        return libraryNotificationDate;
    }

    public void setLibraryNotificationDate(String libraryNotificationDate) {
        this.libraryNotificationDate = libraryNotificationDate;
    }

}
