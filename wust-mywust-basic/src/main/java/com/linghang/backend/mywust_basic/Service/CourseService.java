package com.linghang.backend.mywust_basic.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.Dao.CourseRecord;
import com.linghang.backend.mywust_basic.Mapper.CourseRecordMapper;
import cn.wustlinghang.mywust.data.common.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRecordMapper courseRecordMapper;

    @Autowired
    private com.linghang.backend.mywust_basic.Mapper.ClassroomOccupationMapper occupationMapper;

    @Autowired
    private com.linghang.backend.mywust_basic.Mapper.SchoolClassroomMapper schoolClassroomMapper;

    /**
     * 批量保存课程信息，自动去重，并增加学期审核
     */
    public void saveCourses(List<Course> courses, String term, String currentTerm) {
        if (courses == null || courses.isEmpty()) return;
        
        // 学期审核：只有是当前学期的课程才收录
        if (!term.equals(currentTerm)) {
            return;
        }

        for (Course course : courses) {
            CourseRecord record = CourseRecord.builder()
                    .name(course.getName())
                    .teacher(course.getTeacher())
                    .teachClass(course.getTeachClass())
                    .startWeek(course.getStartWeek())
                    .endWeek(course.getEndWeek())
                    .weekDay(course.getWeekDay())
                    .startSection(course.getStartSection())
                    .endSection(course.getEndSection())
                    .classroom(course.getClassroom())
                    .term(term)
                    .build();

            // 简单去重：检查是否存在完全相同的记录
            QueryWrapper<CourseRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", record.getName())
                    .eq("teacher", record.getTeacher())
                    .eq("teach_class", record.getTeachClass())
                    .eq("start_week", record.getStartWeek())
                    .eq("end_week", record.getEndWeek())
                    .eq("week_day", record.getWeekDay())
                    .eq("start_section", record.getStartSection())
                    .eq("end_section", record.getEndSection())
                    .eq("classroom", record.getClassroom())
                    .eq("term", record.getTerm());

            if (courseRecordMapper.selectCount(queryWrapper) == 0) {
                courseRecordMapper.insert(record);
            }
        }
    }

    /**
     * 蹭课搜索：支持课程名、教师、教室、时间的多重筛选
     */
    public List<CourseRecord> searchCourses(String term, String name, String teacher, String classroom, Integer weekDay, Integer section) {
        QueryWrapper<CourseRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("term", term);
        
        if (name != null && !name.isEmpty()) queryWrapper.like("name", name);
        if (teacher != null && !teacher.isEmpty()) queryWrapper.like("teacher", teacher);
        if (classroom != null && !classroom.isEmpty()) queryWrapper.like("classroom", classroom);
        if (weekDay != null) queryWrapper.eq("week_day", weekDay);
        if (section != null) {
            queryWrapper.le("start_section", section).ge("end_section", section);
        }
        
        queryWrapper.orderByAsc("week_day", "start_section");
        return courseRecordMapper.selectList(queryWrapper);
    }

    /**
     * 查询空教室
     * @param term 学期
     * @param week 周次
     * @param weekDay 星期
     * @param section 节次
     * @return 空教室列表
     */
    public List<String> getEmptyClassrooms(String term, int week, int weekDay, int section) {
        // 1. 获取全校正式注册的所有教室（不再依赖随机抓取的课表）
        List<String> allClassrooms = schoolClassroomMapper.getMasterClassroomList();
        
        // 如果数据库没有导入正式教室，降级回使用课表发现的教室
        if (allClassrooms.isEmpty()) {
            allClassrooms = courseRecordMapper.getAllClassrooms();
        }
        
        // 2. 查询该时间段被【课程】占用的教室
        QueryWrapper<CourseRecord> q1 = new QueryWrapper<>();
        // ... (rest of search logic same)
        q1.eq("term", term)
                .eq("week_day", weekDay)
                .le("start_week", week)
                .ge("end_week", week)
                .le("start_section", section)
                .ge("end_section", section);
        
        Set<String> occupied = courseRecordMapper.selectList(q1).stream()
                .map(CourseRecord::getClassroom)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());
        
        // 3. 查询该时间段被【管理员手动】占用的教室/楼栋
        QueryWrapper<com.linghang.backend.mywust_basic.Dao.ClassroomOccupation> q2 = new QueryWrapper<>();
        q2.eq("term", term)
                .eq("week_day", weekDay)
                .le("start_week", week)
                .ge("end_week", week)
                .le("start_section", section)
                .ge("end_section", section);
        
        List<com.linghang.backend.mywust_basic.Dao.ClassroomOccupation> manualOccs = occupationMapper.selectList(q2);
        
        // 4. 计算并集并排除
        return allClassrooms.stream()
                .filter(c -> !occupied.contains(c))
                .filter(c -> {
                    for (com.linghang.backend.mywust_basic.Dao.ClassroomOccupation mo : manualOccs) {
                        if (c.startsWith(mo.getClassroom())) return false;
                    }
                    return true;
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 基础教室库管理
     */
    public int addSchoolClassroom(com.linghang.backend.mywust_basic.Dao.SchoolClassroom sc) {
        sc.setFullName(sc.getBuilding() + "-" + sc.getRoomNumber());
        return schoolClassroomMapper.insert(sc);
    }

    public List<com.linghang.backend.mywust_basic.Dao.SchoolClassroom> listSchoolClassrooms(String building) {
        QueryWrapper<com.linghang.backend.mywust_basic.Dao.SchoolClassroom> qw = new QueryWrapper<>();
        if (building != null && !building.isEmpty()) qw.eq("building", building);
        qw.orderByAsc("building", "room_number");
        return schoolClassroomMapper.selectList(qw);
    }

    public int deleteSchoolClassroom(Long id) {
        return schoolClassroomMapper.deleteById(id);
    }

    public List<String> listBuildings() {
        return schoolClassroomMapper.getAllBuildings();
    }

    /**
     * 手动占用管理
     */
    public int addOccupation(com.linghang.backend.mywust_basic.Dao.ClassroomOccupation occ) {
        return occupationMapper.insert(occ);
    }

    public List<com.linghang.backend.mywust_basic.Dao.ClassroomOccupation> listOccupations(String term) {
        QueryWrapper<com.linghang.backend.mywust_basic.Dao.ClassroomOccupation> qw = new QueryWrapper<>();
        if (term != null) qw.eq("term", term);
        return occupationMapper.selectList(qw);
    }

    public int deleteOccupation(Long id) {
        return occupationMapper.deleteById(id);
    }
}
