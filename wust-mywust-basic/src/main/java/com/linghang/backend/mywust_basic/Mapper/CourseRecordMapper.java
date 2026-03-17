package com.linghang.backend.mywust_basic.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linghang.backend.mywust_basic.Dao.CourseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseRecordMapper extends BaseMapper<CourseRecord> {
    
    @Select("SELECT DISTINCT classroom FROM course_record WHERE classroom IS NOT NULL AND classroom != ''")
    List<String> getAllClassrooms();
}
