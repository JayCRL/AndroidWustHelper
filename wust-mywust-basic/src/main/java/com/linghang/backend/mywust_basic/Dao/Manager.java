package com.linghang.backend.mywust_basic.Dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// Manager.java
@Data
@TableName("manager")
public class Manager {
    @TableId(type = IdType.AUTO)
    private Long uid; // 学生ID，对应数据库uid字段
    private String studentId; // 学号
    private String password; //密码
    private Integer status;//账号状态
    /**
     * 角色：0-普通管理员, 1-超级管理员
     */
    private Integer role;
}