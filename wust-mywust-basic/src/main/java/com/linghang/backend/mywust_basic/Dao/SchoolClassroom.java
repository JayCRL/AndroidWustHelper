package com.linghang.backend.mywust_basic.Dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("school_classroom")
public class SchoolClassroom {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String building;    // 教学楼
    private String roomNumber;  // 教室号
    private String fullName;    // 全称 (building + "-" + roomNumber)
    private String type;        // 类型
}
