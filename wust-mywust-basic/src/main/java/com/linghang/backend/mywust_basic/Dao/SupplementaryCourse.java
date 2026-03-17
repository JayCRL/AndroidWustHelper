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
@TableName("supplementary_course")
public class SupplementaryCourse {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String name;
    private String teacher;
    private String classroom;
    private Integer startWeek;
    private Integer endWeek;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private String term;
}
