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
@TableName("classroom_occupation")
public class ClassroomOccupation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String classroom; // 教室名称或教学楼前缀
    private String term;
    private Integer startWeek;
    private Integer endWeek;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private String reason;
    private String college;
}
