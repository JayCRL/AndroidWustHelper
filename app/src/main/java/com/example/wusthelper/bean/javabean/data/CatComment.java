package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CatComment {
    public int id;
    public String authorName;
    public String content;
    public String createdAt;

    public static List<CatComment> mockCommentsFor(CatPost post) {
        List<CatComment> list = new ArrayList<>();
        CatComment first = new CatComment();
        first.id = 1;
        first.authorName = "热心同学";
        first.content = "好可爱，最近我也在图书馆附近见过它。";
        first.createdAt = "刚刚";
        list.add(first);

        CatComment second = new CatComment();
        second.id = 2;
        second.authorName = "喵星观察员";
        second.content = post != null && post.tags != null && post.tags.contains(CatTag.HELP)
                ? "建议先联系学校保卫处或附近宠物医院。"
                : "谢谢分享，今天心情都变好了。";
        second.createdAt = "1小时前";
        list.add(second);
        return list;
    }
}
