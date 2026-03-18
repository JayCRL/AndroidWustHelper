package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CatPost {
    public String id;
    public String title;
    public String content;
    public List<String> images = new ArrayList<>();
    public String authorName;
    public String authorAvatar;
    public List<String> tags = new ArrayList<>();
    public int likes;
    public int commentsCount;
    public String createDate;
}
