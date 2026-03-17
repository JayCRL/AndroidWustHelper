package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Tag(name = "日志管理接口", description = "提供日志查看等操作的接口")
@RequestMapping("/LogController")
public class LogController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    OperationService oprationService;

    @Autowired
    private com.linghang.backend.mywust_basic.Mapper.LoginLogMapper loginLogMapper;

    static {
        logger.info("日志控制器注册成功！！！");
    }
    //日志的查看
    /**
     * 日志的查看
     *
     * @return 日志集合List<OperationLog></>
     */
    @GetMapping("/list")
    @Operation(summary = "查看日志", description = "查看操作日志")
    public R<List<OperationLog>> list(){
        return  R.success(oprationService.list());
    }

    @GetMapping("/loginLogs")
    @Operation(summary = "查看登录日志", description = "查看所有用户（学生和管理员）的登录尝试记录，支持按学号搜索")
    public R<List<com.linghang.backend.mywust_basic.Dao.LoginLog>> listLoginLogs(@RequestParam(value = "username", required = false) String username) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.linghang.backend.mywust_basic.Dao.LoginLog> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("login_time");
        return R.success(loginLogMapper.selectList(queryWrapper));
    }

    /**
     * 获取学生状态概览（每个学生的最后一次登录记录）
     */
    @GetMapping("/studentStatus")
    @Operation(summary = "学生状态概览", description = "获取每个学生最后一次的登录状态，支持按学号搜索")
    public R<List<com.linghang.backend.mywust_basic.Dao.LoginLog>> getStudentStatus(@RequestParam(value = "username", required = false) String username) {
        // 使用 MyBatis-Plus 的 QueryWrapper 配合自定义 SQL 或 分组查询
        // 这里采用一种兼容性较好的方式：查询每个学号最大的 ID 对应的记录
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.linghang.backend.mywust_basic.Dao.LoginLog> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.inSql("id", "SELECT MAX(id) FROM login_log WHERE type = 'STUDENT' GROUP BY username");
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("login_time");
        return R.success(loginLogMapper.selectList(queryWrapper));
    }
}
