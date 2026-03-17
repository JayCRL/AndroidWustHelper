package com.linghang.backend.mywust_basic.Controller;

import cn.wustlinghang.mywust.common.core.Result;
import cn.wustlinghang.mywust.common.security.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.BusinessException;
import com.linghang.backend.mywust_basic.Dao.LoginLog;
import com.linghang.backend.mywust_basic.Dao.Manager;
import com.linghang.backend.mywust_basic.Mapper.LoginLogMapper;
import com.linghang.backend.mywust_basic.Mapper.ManagerMapper;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.AesEncryptor;
import com.linghang.backend.mywust_basic.dto.ManagerDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String LOGIN_TRANSPORT_PREFIX = "ENC:";
    private static final String LOGIN_TRANSPORT_KEY_BASE64 = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired
    private ManagerMapper managerMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AesEncryptor aesEncryptor;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Data
    public static class LoginDto {
        @NotBlank(message = "用户名（学号）不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    private static class LoginResponse {
        private String token;
        private String username;
        private Integer role;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginDto loginDto) throws Exception {
        String username = loginDto.getUsername();
        String decryptedPassword = decodeTransportPassword(loginDto.getPassword());
        try {
            QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_id", username);
            Manager manager = managerMapper.selectOne(queryWrapper);

            if (manager == null) {
                throw new BusinessException("用户名不存在");
            }
            if (!aesEncryptor.matches(decryptedPassword, manager.getPassword())) {
                throw new BusinessException("密码错误");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", manager.getStudentId());
            String token = jwtUtils.generateToken(claims);

            tokenService.createAdminUsername(token, loginDto.getUsername());
            tokenService.createAdminRole(token, manager.getRole());

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(manager.getStudentId());
            response.setRole(manager.getRole());

            try {
                loginLogMapper.insert(new LoginLog(username, "ADMIN", "SUCCESS", "登录成功"));
            } catch (Exception logEx) {
                logger.warn("写入登录成功日志失败", logEx);
            }

            return Result.ok(response);
        } catch (Exception e) {
            String errorMsg = (e instanceof BusinessException) ? e.getMessage() : "系统内部错误";
            try {
                loginLogMapper.insert(new LoginLog(username, "ADMIN", "FAILURE", errorMsg));
            } catch (Exception logEx) {
                logger.warn("写入登录失败日志失败", logEx);
            }
            throw e;
        }
    }

    private String decodeTransportPassword(String password) {
        if (password == null || !password.startsWith(LOGIN_TRANSPORT_PREFIX)) {
            return password;
        }
        try {
            String payload = password.substring(LOGIN_TRANSPORT_PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(payload);
            byte[] iv = Arrays.copyOfRange(combined, 0, 12);
            byte[] ciphertext = Arrays.copyOfRange(combined, 12, combined.length);

            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(LOGIN_TRANSPORT_KEY_BASE64), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("登录传输密码解密失败，按明文兜底", e);
            return password;
        }
    }

    @PostMapping("/manager/create")
    public Result<String> createManager(@Valid @RequestBody ManagerDto managerDto) throws Exception {
        String token = getToken();
        if (token == null || !Integer.valueOf(1).equals(tokenService.getAdminRole(token))) {
            throw new BusinessException("仅限超级管理员操作");
        }

        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", managerDto.getUsername());
        if (managerMapper.selectOne(queryWrapper) != null) {
            throw new BusinessException("学号已存在");
        }

        Manager manager = new Manager();
        manager.setStudentId(managerDto.getUsername());
        manager.setPassword(aesEncryptor.encrypt(managerDto.getPassword()));
        manager.setStatus(1);
        manager.setRole(0);
        managerMapper.insert(manager);

        return Result.ok("管理员创建成功");
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        String token = getToken();
        if (token != null) {
            tokenService.deleteAdminSession(token);
        }
        return Result.ok();
    }

    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String token) {
            return token;
        }
        return null;
    }
}
