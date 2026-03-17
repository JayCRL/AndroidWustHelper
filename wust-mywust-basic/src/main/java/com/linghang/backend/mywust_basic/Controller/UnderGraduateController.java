package com.linghang.backend.mywust_basic.Controller;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradCourseTableParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradScoreParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradStudentInfoPageParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradTrainingPlanPageParser;
import cn.wustlinghang.mywust.core.request.service.auth.UndergraduateLogin;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradCourseTableApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradScoreApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradStudentInfoApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradTrainingPlanApiService;
import cn.wustlinghang.mywust.core.util.WustRequester;
import cn.wustlinghang.mywust.data.common.Course;
import cn.wustlinghang.mywust.data.common.Score;
import cn.wustlinghang.mywust.data.common.StudentInfo;
import com.linghang.backend.mywust_basic.Entity.UnderGraduateLoginA;
import com.linghang.backend.mywust_basic.Service.PictureService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.*;
import com.linghang.backend.mywust_basic.dto.DataInformation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Tag(name = "本科生接口", description = "提供课程、成绩、考试等功能接口")
@RestController
@RequestMapping("/UnderGraduateStudent")
public class UnderGraduateController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    private TokenService tokenService;

    @Autowired
    private WustRequester wustRequester;
    @Autowired
    private UndergraduateLogin undergraduateLogin;

    @Autowired
    private com.linghang.backend.mywust_basic.Mapper.LoginLogMapper loginLogMapper;

    @Autowired
    private com.linghang.backend.mywust_basic.Service.CourseService courseService;

    @Autowired
    private com.linghang.backend.mywust_basic.Service.SupplementaryCourseService supplementaryCourseService;

    //自动注入
    static {
        logger.info("本科生控制器注册成功！！！");
    }
    @Value("${WustHelper.term}")
    String this_term;
    @Value("${StartDay.Year}")
    Integer year;
    @Value("${StartDay.Month}")
    Integer month;
    @Value("${StartDay.Day}")
    Integer day;

    @Autowired
    PictureService pictureService;

    @Operation(summary = "添加补充课表", description = "用户手动添加自己的课表项")
    @PostMapping("/addSupplementaryCourse")
    public R<String> addSupplementaryCourse(@RequestBody com.linghang.backend.mywust_basic.Dao.SupplementaryCourse course) {
        String username = getUsernameFromContext();
        course.setUsername(username);
        if (course.getTerm() == null) course.setTerm(this_term);
        try {
            supplementaryCourseService.addCourse(course);
            return R.success("success");
        } catch (Exception e) {
            logger.error("添加补充课表失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "更新补充课表", description = "用户修改自己手动添加的课表项")
    @PostMapping("/updateSupplementaryCourse")
    public R<String> updateSupplementaryCourse(@RequestBody com.linghang.backend.mywust_basic.Dao.SupplementaryCourse course) {
        String username = getUsernameFromContext();
        // 简单校验：只能更新自己的
        course.setUsername(username); 
        try {
            supplementaryCourseService.updateCourse(course);
            return R.success("success");
        } catch (Exception e) {
            logger.error("更新补充课表失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "删除补充课表", description = "用户删除自己手动添加的课表项")
    @PostMapping("/deleteSupplementaryCourse")
    public R<String> deleteSupplementaryCourse(@RequestParam("id") Long id) {
        String username = getUsernameFromContext();
        try {
            supplementaryCourseService.deleteCourse(id, username);
            return R.success("success");
        } catch (Exception e) {
            logger.error("删除补充课表失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取补充课表列表", description = "获取当前用户手动添加的全部课表项")
    @GetMapping("/listSupplementaryCourses")
    public R<List<com.linghang.backend.mywust_basic.Dao.SupplementaryCourse>> listSupplementaryCourses(@RequestParam(value = "term", required = false) String term) {
        String username = getUsernameFromContext();
        String queryTerm = (term != null) ? term : this_term;
        try {
            return R.success(supplementaryCourseService.listCourses(username, queryTerm));
        } catch (Exception e) {
            logger.error("获取补充课表失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "聚合登录接口", description = "登录教务系统并返回本系统Token")
    @PostMapping("/login")
    public R<String> login(@RequestBody UnderGraduateLoginA loginA) {
        String username = loginA.getUsername();
        String password = loginA.getPassword();
        try {
            // 1. 登录真实教务系统获取 Cookie
            String cookie = undergraduateLogin.getLoginCookie(username, password, null);
            
            // 2. 生成本系统 Token
            String token = tokenService.createToken(username);
            
            // 3. 将教务系统 Cookie 存入缓存
            tokenService.createWustCookie(username, cookie);
            
            // 4. 获取并缓存姓名信息 (可选，为了getName接口能用)
            UndergradStudentInfoApiService studentInfoApiService = new UndergradStudentInfoApiService(wustRequester);
            String page = studentInfoApiService.getPage(cookie);
            StudentInfo studentInfo = new UndergradStudentInfoPageParser().parse(page);
            tokenService.createName(token, studentInfo.getName());
            
            // 5. 记录登录日志
            try {
                loginLogMapper.insert(new com.linghang.backend.mywust_basic.Dao.LoginLog(username, "STUDENT", "SUCCESS", "登录成功"));
            } catch (Exception e) {
                logger.error("记录登录日志失败", e);
            }

            logger.info("学号：{} 登录并缓存成功", username);
            return R.success(token);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            // 记录失败日志
            try {
                loginLogMapper.insert(new com.linghang.backend.mywust_basic.Dao.LoginLog(username, "STUDENT", "FAILURE", errorMsg));
            } catch (Exception logEx) {
                logger.error("记录登录失败日志时出错", logEx);
            }
            logger.error("学号：{} 登录失败：{}", username, errorMsg);
            return R.failure(500, errorMsg);
        }
    }

    @Operation(summary = "获取个人信息", description = "直接返回解析后的个人信息")
    @GetMapping("/getStudentInfo")
    public R<StudentInfo> getStudentInfo() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        try {
            UndergradStudentInfoApiService apiService = new UndergradStudentInfoApiService(wustRequester);
            String page = apiService.getPage(cookie);
            StudentInfo studentInfo = new UndergradStudentInfoPageParser().parse(page);
            return R.success(studentInfo);
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取本学期课程表", description = "直接返回解析后的课程列表")
    @GetMapping("/getCourses")
    public R<List<Course>> getCourses(@RequestParam(value = "term", required = false) String term) {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        String queryTerm = (term != null) ? term : this_term;
        try {
            UndergradCourseTableApiService apiService = new UndergradCourseTableApiService(wustRequester);
            String page = apiService.getPage(queryTerm, cookie);
            List<Course> courseList = new UndergradCourseTableParser().parse(page);
            
            // 后台维护总课程/教室数据库
            try {
                courseService.saveCourses(courseList, queryTerm, this_term);
            } catch (Exception e) {
                logger.error("后台维护课程数据失败", e);
            }
            
            return R.success(courseList);
        } catch (Exception e) {
            logger.error("获取课程表失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "查询空教室", description = "根据学期、周次、星期、节次查询空闲教室")
    @GetMapping("/getEmptyClassrooms")
    public R<List<String>> getEmptyClassrooms(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam("week") Integer week,
            @RequestParam("weekDay") Integer weekDay,
            @RequestParam("section") Integer section) {
        
        String queryTerm = (term != null) ? term : this_term;
        try {
            List<String> emptyClassrooms = courseService.getEmptyClassrooms(queryTerm, week, weekDay, section);
            return R.success(emptyClassrooms);
        } catch (Exception e) {
            logger.error("查询空教室失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "蹭课查询", description = "支持按课程名、教师、教室、星期、节次进行搜索")
    @GetMapping("/searchCourses")
    public R<List<com.linghang.backend.mywust_basic.Dao.CourseRecord>> searchCourses(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "teacher", required = false) String teacher,
            @RequestParam(value = "classroom", required = false) String classroom,
            @RequestParam(value = "weekDay", required = false) Integer weekDay,
            @RequestParam(value = "section", required = false) Integer section) {
        
        String queryTerm = (term != null) ? term : this_term;
        try {
            return R.success(courseService.searchCourses(queryTerm, name, teacher, classroom, weekDay, section));
        } catch (Exception e) {
            logger.error("搜索课程失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "手动占用教室", description = "管理员手动标记教室、楼栋或学院为占用状态")
    @PostMapping("/addOccupation")
    public R<String> addOccupation(@RequestBody com.linghang.backend.mywust_basic.Dao.ClassroomOccupation occ) {
        if (occ.getTerm() == null) occ.setTerm(this_term);
        try {
            courseService.addOccupation(occ);
            return R.success("success");
        } catch (Exception e) {
            logger.error("手动占用教室失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "列出手动占用记录")
    @GetMapping("/listOccupations")
    public R<List<com.linghang.backend.mywust_basic.Dao.ClassroomOccupation>> listOccupations(@RequestParam(value = "term", required = false) String term) {
        String queryTerm = (term != null) ? term : this_term;
        return R.success(courseService.listOccupations(queryTerm));
    }

    @Operation(summary = "删除手动占用记录")
    @PostMapping("/deleteOccupation")
    public R<String> deleteOccupation(@RequestParam("id") Long id) {
        try {
            courseService.deleteOccupation(id);
            return R.success("success");
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "添加基础教室信息")
    @PostMapping("/addSchoolClassroom")
    public R<String> addSchoolClassroom(@RequestBody com.linghang.backend.mywust_basic.Dao.SchoolClassroom sc) {
        try {
            courseService.addSchoolClassroom(sc);
            return R.success("success");
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取教学楼列表")
    @GetMapping("/listBuildings")
    public R<List<String>> listBuildings() {
        return R.success(courseService.listBuildings());
    }

    @Operation(summary = "获取全校教室列表")
    @GetMapping("/listSchoolClassrooms")
    public R<List<com.linghang.backend.mywust_basic.Dao.SchoolClassroom>> listSchoolClassrooms(@RequestParam(value = "building", required = false) String building) {
        return R.success(courseService.listSchoolClassrooms(building));
    }

    @Operation(summary = "删除教室信息")
    @PostMapping("/deleteSchoolClassroom")
    public R<String> deleteSchoolClassroom(@RequestParam("id") Long id) {
        try {
            courseService.deleteSchoolClassroom(id);
            return R.success("success");
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取成绩", description = "直接返回解析后的成绩列表")
    @GetMapping("/getScores")
    public R<List<Score>> getScores() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        try {
            UndergradScoreApiService apiService = new UndergradScoreApiService(wustRequester);
            String page = apiService.getPage(cookie);
            List<Score> scoreList = new UndergradScoreParser().parse(page);
            return R.success(scoreList);
        } catch (Exception e) {
            logger.error("获取成绩失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取培养方案", description = "返回解析后的培养方案内容")
    @GetMapping("/getTrainingPlan")
    public R<String> getTrainingPlan() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        try {
            UndergradTrainingPlanApiService apiService = new UndergradTrainingPlanApiService(wustRequester);
            String page = apiService.getPage(cookie);
            String plan = new UndergradTrainingPlanPageParser().parse(page);
            return R.success(plan);
        } catch (Exception e) {
            logger.error("获取培养方案失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取毕业要求情况", description = "直接返回解析后的毕业要求及完成情况")
    @GetMapping("/getGraduateRequire")
    public R<Map<String, Object>> getGraduateRequire() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        try {
            String page = CreditStatusPageGet.GetPage(cookie);
            Map<String, Object> graduateRequire = GraduateRequireParser.Parse(page);
            return R.success(graduateRequire);
        } catch (Exception e) {
            logger.error("获取毕业要求失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取考试安排", description = "返回指定学期的考试安排页面")
    @GetMapping("/getExam")
    public R<String> getExam(@RequestParam(value = "term", required = false) String term) {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "教务系统会话失效，请重新登录");
        
        String queryTerm = (term != null) ? term : this_term;
        try {
            return R.success(ExamFetcher.GetExamPage(queryTerm, cookie));
        } catch (Exception e) {
            logger.error("获取考试安排失败", e);
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取本学期起始日", description = "获取此学期起始日信息")
    @GetMapping("/getData")
    public R<DataInformation> GetData(){
        DataInformation dataInformation=new DataInformation();
        dataInformation.setYear(year);
        dataInformation.setMonth(month);
        dataInformation.setDay(day);
        return R.success(dataInformation);
    }

    private String getUsernameFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString(); // JwtAuthenticationFilter set uid as principal
    }
}
