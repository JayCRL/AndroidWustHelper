package com.linghang.backend.mywust_basic.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linghang.backend.mywust_basic.Dao.SchoolClassroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SchoolClassroomMapper extends BaseMapper<SchoolClassroom> {
    
    @Select("SELECT full_name FROM school_classroom ORDER BY building, room_number")
    List<String> getMasterClassroomList();
    
    @Select("SELECT DISTINCT building FROM school_classroom ORDER BY building")
    List<String> getAllBuildings();
}
