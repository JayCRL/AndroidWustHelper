package com.linghang.backend.mywust_basic.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.Dao.Manager;
import com.linghang.backend.mywust_basic.Mapper.ManagerMapper;
import com.linghang.backend.mywust_basic.Utils.AesEncryptor;
import com.linghang.backend.mywust_basic.dto.ManagerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 注意：该方法建议放在 Service 层（如 ManagerService）中，而非 Controller 层
@Service
public class ManagerService {
    @Autowired
    AesEncryptor aesEncryptor;
    // 注入 ManagerMapper（MyBatis-Plus 自动生成的 Mapper 接口）
    @Autowired
    private ManagerMapper managerMapper;

    /**
     * 核验管理员密码（根据学号查询，对比密码）
     * @param managerDto 前端传入的DTO（包含用户名/学号、密码）
     * @return 密码核验结果：true=成功，false=失败（用户不存在或密码错误）
     */
    public boolean checkPassword(ManagerDto managerDto) throws Exception {
        // 1. 构建查询条件：根据 DTO 中的 username（对应数据库 student_id）查询
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        // 注意：确保 DTO 的 username 对应数据库的 student_id（字段名需与数据库一致）
        queryWrapper.eq("student_id", managerDto.getUsername());
        // 2. 执行查询：根据学号查询唯一的管理员记录（需确保 student_id 在数据库是唯一键）
        // 避免使用 selectByMap()：selectByMap 需要传 Map<String, Object>，且返回 List<Manager>，不适合单条查询
        Manager dbManager = managerMapper.selectOne(queryWrapper);
        // 3. 处理查询结果，核验密码
        if (dbManager == null) {
            // 情况1：未查询到对应学号的管理员 → 核验失败
            return false;
        }
        // 情况2：查询到管理员 → 对比密码（注意：避免空指针，用数据库密码调用 equals）
        // 加密存储密码
        return dbManager.getPassword().equals(aesEncryptor.decrypt(managerDto.getPassword()));
    }
    public boolean ifExsists(ManagerDto managerDto) throws Exception {
        // 1. 构建查询条件：根据 DTO 中的 username（对应数据库 student_id）查询
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        // 注意：确保 DTO 的 username 对应数据库的 student_id（字段名需与数据库一致）
        queryWrapper.eq("student_id", managerDto.getUsername());
        // 2. 执行查询：根据学号查询唯一的管理员记录（需确保 student_id 在数据库是唯一键）
        // 避免使用 selectByMap()：selectByMap 需要传 Map<String, Object>，且返回 List<Manager>，不适合单条查询
        Manager dbManager = managerMapper.selectOne(queryWrapper);
        // 3. 处理查询结果，核验密码
        if (dbManager == null) {
            // 情况1：未查询到对应学号的管理员 → 核验失败
            return false;
        }
      return true;
    }

    public boolean setPassword(ManagerDto managerDto) throws Exception {
        // 1. 构建查询条件：根据 DTO 中的 username（对应数据库 student_id）查询
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        // 注意：确保 DTO 的 username 对应数据库的 student_id（字段名需与数据库一致）
        queryWrapper.eq("student_id", managerDto.getUsername());
        // 2. 执行查询：根据学号查询唯一的管理员记录（需确保 student_id 在数据库是唯一键）
        // 避免使用 selectByMap()：selectByMap 需要传 Map<String, Object>，且返回 List<Manager>，不适合单条查询
        Manager dbManager = managerMapper.selectOne(queryWrapper);
        // 3. 处理查询结果，核验密码
        if (dbManager == null) {
            // 情况1：未查询到对应学号的管理员 → 核验失败
            System.out.println("123");
            return false;
        }
        //设置加密后的密码
        dbManager.setPassword(aesEncryptor.encrypt(managerDto.getPassword()));
        managerMapper.updateById(dbManager);
        System.out.println("123");
        return  true;
    }
}