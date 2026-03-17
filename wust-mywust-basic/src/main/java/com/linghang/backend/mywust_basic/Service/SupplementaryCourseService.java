package com.linghang.backend.mywust_basic.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.Dao.SupplementaryCourse;
import com.linghang.backend.mywust_basic.Mapper.SupplementaryCourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplementaryCourseService {

    @Autowired
    private SupplementaryCourseMapper supplementaryCourseMapper;

    public int addCourse(SupplementaryCourse course) {
        return supplementaryCourseMapper.insert(course);
    }

    public int updateCourse(SupplementaryCourse course) {
        return supplementaryCourseMapper.updateById(course);
    }

    public int deleteCourse(Long id, String username) {
        QueryWrapper<SupplementaryCourse> qw = new QueryWrapper<>();
        qw.eq("id", id).eq("username", username);
        return supplementaryCourseMapper.delete(qw);
    }

    public List<SupplementaryCourse> listCourses(String username, String term) {
        QueryWrapper<SupplementaryCourse> qw = new QueryWrapper<>();
        qw.eq("username", username);
        if (term != null && !term.isEmpty()) {
            qw.eq("term", term);
        }
        return supplementaryCourseMapper.selectList(qw);
    }
}
