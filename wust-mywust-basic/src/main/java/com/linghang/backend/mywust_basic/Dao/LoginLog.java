package com.linghang.backend.mywust_basic.Dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@TableName("login_log")
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String type; // ADMIN or STUDENT
    
    private Date loginTime;
    
    private String status; // SUCCESS or FAILURE (though only success is requested)
    
    private String message; // 记录具体的返回消息，如 "密码错误"、"用户被封禁"

    public LoginLog(String username, String type, String status, String message) {
        this.username = username;
        this.type = type;
        this.status = status;
        this.message = message;
        this.loginTime = new Date();
    }
}
