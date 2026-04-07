package com.example.wusthelper.request;

import com.example.wusthelper.bean.javabean.data.CatPost;
import com.example.wusthelper.bean.javabean.data.CatTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CampusCatDataProvider {

    private CampusCatDataProvider() {
    }

    public static List<CatPost> getPosts() {
        List<CatPost> posts = new ArrayList<>();
        posts.add(createPost("图书馆门口的小花猫", "今天在图书馆门口偶遇这只小可爱，一点都不怕人，还蹭我的腿！有没有人知道它的名字呀？", "爱猫的学姐", Arrays.asList(CatTag.STRAY, CatTag.DAILY), 128, 32, "1小时前"));
        posts.add(createPost("紧急求助！", "在南苑食堂后面发现一只受伤的小猫，腿好像断了，有没有懂救助的同学帮忙看看？", "热心同学", Arrays.asList(CatTag.HELP, CatTag.STRAY), 45, 12, "1天前"));
        posts.add(createPost("求领养：三花妹妹", "室友猫毛过敏实在养不了了，找个好心人领养。三花妹妹，3个月大，已驱虫，未绝育。", "找铲屎官", Arrays.asList(CatTag.ADOPT), 89, 56, "2天前"));
        posts.add(createPost("今日份的快乐", "看这睡姿，也是没谁了哈哈哈。", "橘猫大队长", Arrays.asList(CatTag.SHOW_OFF, CatTag.DAILY), 233, 15, "5分钟前"));
        posts.add(createPost("关于校园流浪猫绝育计划", "我们将于本周末开展新一轮的TNR行动，欢迎大家报名志愿者！", "动保协会", Arrays.asList(CatTag.STRAY, CatTag.HELP), 567, 88, "5天前"));
        return posts;
    }

    private static CatPost createPost(String title, String content, String authorName, List<String> tags,
                                      int likes, int commentsCount, String createDate) {
        CatPost post = new CatPost();
        post.id = UUID.randomUUID().toString();
        post.title = title;
        post.content = content;
        post.authorName = authorName;
        post.authorAvatar = "person.crop.circle.fill";
        post.tags = new ArrayList<>(tags);
        post.likes = likes;
        post.commentsCount = commentsCount;
        post.createDate = createDate;
        return post;
    }
}
