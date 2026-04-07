package com.example.wusthelper.bean.javabean.data;

import java.io.Serializable;

public class Commodity implements Serializable {
    public int pid;
    public String uid;
    public String name;
    public double price;
    public String date;
    public String contact;
    public int status;
    public int type;
    public String introduce;
    public Integer image_id;

    public String getStatusDescription() {
        switch (status) {
            case 0:
                return "出售";
            case 1:
                return "求购";
            default:
                return "其他";
        }
    }
}
