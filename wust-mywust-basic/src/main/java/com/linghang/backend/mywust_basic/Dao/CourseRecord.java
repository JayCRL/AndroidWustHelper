package com.linghang.backend.mywust_basic.Dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("course_record")
public class CourseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String teacher;
    private String teachClass;
    private Integer startWeek;
    private Integer endWeek;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;
    private String classroom;
    private String term;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseRecord that = (CourseRecord) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(teacher, that.teacher) &&
                Objects.equals(teachClass, that.teachClass) &&
                Objects.equals(startWeek, that.startWeek) &&
                Objects.equals(endWeek, that.endWeek) &&
                Objects.equals(weekDay, that.weekDay) &&
                Objects.equals(startSection, that.startSection) &&
                Objects.equals(endSection, that.endSection) &&
                Objects.equals(classroom, that.classroom) &&
                Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, teacher, teachClass, startWeek, endWeek, weekDay, startSection, endSection, classroom, term);
    }
}
