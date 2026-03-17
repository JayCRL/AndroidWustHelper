/*
 * @author yuandalai
 * @date 2018/11/14
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class SearchBookBean {

    @SerializedName("detailUrl")
    private String detailUrl;

    @SerializedName("title")
    private String bookName;

    @SerializedName("author")
    private String author;

    @SerializedName("publisher")
    private String press;

    @SerializedName("allNum")
    private String sum;

    @SerializedName("remainNum")
    private String borrowableNum;

    @SerializedName("imgUrl")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
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

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getBorrowableNum() {
        return borrowableNum;
    }

    public void setBorrowableNum(String borrowableNum) {
        this.borrowableNum = borrowableNum;
    }

}
