package com.linghang.backend.mywust_basic.Controller;

import cn.wustlinghang.mywust.core.parser.graduate.GraduateCourseTableParser;
import cn.wustlinghang.mywust.core.parser.graduate.GraduateStudentInfoPageParser;
import cn.wustlinghang.mywust.core.parser.graduate.GraduateTrainingPlanPageParser;
import cn.wustlinghang.mywust.core.request.service.auth.GraduateLogin;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateCourseTableApiService;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateStudentInfoApiService;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateTrainingPlanApiService;
import cn.wustlinghang.mywust.core.util.WustRequester;
import cn.wustlinghang.mywust.data.common.Course;
import cn.wustlinghang.mywust.data.common.StudentInfo;
import com.linghang.backend.mywust_basic.Entity.UnderGraduateLoginA;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.R;
import com.linghang.backend.mywust_basic.dto.DataInformation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "研究生接口", description = "提供课程、成绩、考试等功能接口")

@RestController
@RequestMapping("/GraduatedController")
public class GraduatedController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);

    @Autowired
    private TokenService tokenService;
    @Autowired
    private WustRequester wustRequester;
    @Autowired
    private GraduateLogin graduateLogin;

    @Operation(summary = "研究生聚合登录", description = "登录教务系统并返回本系统Token")
    @PostMapping("/login")
    public R<String> login(@RequestBody UnderGraduateLoginA loginA) {
        try {
            String username = loginA.getUsername();
            String password = loginA.getPassword();
            // 1. 登录
            String cookie = graduateLogin.getLoginCookie(username, password, null);
            
            // 2. 生成 Token
            String token = tokenService.createToken(username);
            
            // 3. 缓存 Cookie
            tokenService.createWustCookie(username, cookie);
            
            // 4. 获取并缓存姓名
            GraduateStudentInfoApiService apiService = new GraduateStudentInfoApiService(wustRequester);
            String page = apiService.getPage(cookie, null);
            StudentInfo info = new GraduateStudentInfoPageParser().parse(page);
            tokenService.createName(token, info.getName());
            
            logger.info("学号：{} 研究生登录成功", username);
            return R.success(token);
        } catch (Exception e) {
            logger.error("研究生登录失败", e);
            return R.failure(500, "登录失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取个人信息", description = "直接返回解析后的个人信息")
    @GetMapping("/getStudentInfo")
    public R<StudentInfo> getStudentInfo() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "会话失效");
        
        try {
            GraduateStudentInfoApiService apiService = new GraduateStudentInfoApiService(wustRequester);
            String page = apiService.getPage(cookie, null);
            StudentInfo studentInfo = new GraduateStudentInfoPageParser().parse(page);
            return R.success(studentInfo);
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取课程表", description = "直接返回解析后的课程列表")
    @GetMapping("/getCourses")
    public R<List<Course>> getCourses() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "会话失效");
        
        try {
            GraduateCourseTableApiService apiService = new GraduateCourseTableApiService(wustRequester);
            String page = apiService.getPage(cookie, null);
            List<Course> courseList = new GraduateCourseTableParser().parse(page);
            return R.success(courseList);
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取培养方案", description = "返回解析后的内容")
    @GetMapping("/getTrainingPlan")
    public R<String> getTrainingPlan() {
        String username = getUsernameFromContext();
        String cookie = tokenService.getWustCookie(username);
        if (cookie == null) return R.failure(401, "会话失效");
        
        try {
            GraduateTrainingPlanApiService apiService = new GraduateTrainingPlanApiService(wustRequester);
            String page = apiService.getPage(cookie, null);
            String plan = new GraduateTrainingPlanPageParser().parse(page);
            return R.success(plan);
        } catch (Exception e) {
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取本学期起始日", description = "获取此学期起始日信息")
    @GetMapping("/getData")
    R<DataInformation> GetData(){
       DataInformation dataInformation=new DataInformation();
       dataInformation.setYear(2025);
       dataInformation.setMonth(9);
       dataInformation.setDay(1);
       return R.success(dataInformation);
    }

    private String getUsernameFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString();
    }

}
