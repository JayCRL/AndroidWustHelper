package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EmptyClassroomBean {

    @SerializedName("floor")
    private String floor;
    @SerializedName("count")
    private String count;
    @SerializedName("rooms")
    private List<String> rooms = new ArrayList<>();

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return "EmptyClassroomBean{" +
                "floor='" + floor + '\'' +
                ", count='" + count + '\'' +
                ", rooms=" + rooms +
                '}';
    }
}
