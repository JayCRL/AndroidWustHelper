package com.linghang.backend.mywust_basic.Service;

import com.linghang.backend.mywust_basic.Utils.AesEncryptor;
import cn.wustlinghang.mywust.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Service
public class TokenService {
    @Autowired
    AesEncryptor aesEncryptor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;
    public TokenService(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }
    // 生成Token并缓存用户获取的cookie，缓存过期时间7天
    public String createToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        String token = jwtUtils.generateToken(claims);
        redisTemplate.opsForValue().set("Wust_basic_token:" + token, userId, 7, TimeUnit.DAYS);
        createUidToken(token,userId);
        return token;
    }
    // 生成Token并缓存用户获取的cookie，缓存过期时间7天
    public boolean createAdminToken(String username, Object token) {
        redisTemplate.opsForValue().set("admin:" + username, token, 7, TimeUnit.DAYS);
        return true;
    }
    /**
     * 刷新token关联的用户信息（不重新创建token，不改变过期时间）
     * @param token 已存在的token
     * @param newUserInfo 新的用户信息（需要更新的值）
     * @return 是否刷新成功（true：token存在且更新成功；false：token不存在）
     */
    public boolean refreshTokenInfo(String token, Object newUserInfo) {
        String redisKey = "Wust_basic_token:" + token;
        // 1. 检查token是否存在（避免更新不存在的token）
        Boolean exists = redisTemplate.hasKey(redisKey);
        if(Boolean.FALSE.equals(exists)){
            return false;
        }
        // 2. 更新Redis中存储的userInfo，不指定过期时间（保留原有过期时间）
        redisTemplate.opsForValue().set(redisKey, newUserInfo);
        return true;
    }
    public String getUserFromToken(String token) {
        return (String) redisTemplate.opsForValue().get("Wust_basic_token:"+token);
    }
    public String getAdminToken(String username) {
        return (String) redisTemplate.opsForValue().get("admin:"+username);
    }
    //学号缓存
    public boolean createUidToken(String token,String username){
        redisTemplate.opsForValue().set("LingHangToken:"+token,username,7, TimeUnit.DAYS);
        return true;
    }
    public boolean createAdminUsername(String token,String username){
        redisTemplate.opsForValue().set("Admin:"+token,username,7, TimeUnit.DAYS);
        return true;
    }
    public boolean createAdminRole(String token, Integer role) {
        redisTemplate.opsForValue().set("AdminRole:" + token, role, 7, TimeUnit.DAYS);
        return true;
    }

    public String getUid(String token){
        return (String) redisTemplate.opsForValue().get("LingHangToken:"+token);
    }
    //姓名缓存
    public boolean createName(String token,String username){
        redisTemplate.opsForValue().set("Student_name:"+token,username,7, TimeUnit.DAYS);
        return true;
    }
    public boolean setPassword(String username,String password) throws Exception {
        password= aesEncryptor.encrypt(password);
        redisTemplate.opsForValue().set("Student_number:"+username,password,7, TimeUnit.DAYS);
        return true;
    }
    public String getPassword(String username) throws Exception {
        return aesEncryptor.decrypt((String)redisTemplate.opsForValue().get("Student_number:"+username));
    }
    public String getName(String token) {
        return (String) redisTemplate.opsForValue().get("Student_name:"+token);
    }

    // 教育系统Cookie缓存
    public boolean createWustCookie(String userId, String cookie) {
        redisTemplate.opsForValue().set("WustCookie:" + userId, cookie, 7, TimeUnit.DAYS);
        return true;
    }

    public String getWustCookie(String userId) {
        return (String) redisTemplate.opsForValue().get("WustCookie:" + userId);
    }

    // 根据token从Redis获取用户信息
    public String getAdminUsername(String token) {
        return (String) redisTemplate.opsForValue().get("Admin:"+token);
    }
    public Integer getAdminRole(String token) {
        Object role = redisTemplate.opsForValue().get("AdminRole:" + token);
        return role != null ? (Integer) role : 0;
    }

    // 删除Redis中保存的token，实现注销
    public void deleteToken(String token) {
        redisTemplate.delete("Wust_basic_token:" + token);
        deleteUid(token);
        deleteName(token);
    }
    public void deleteUid(String token) {
        redisTemplate.delete("LingHangToken:" + token);
    }
    public void deleteName(String token) {
        redisTemplate.delete("Student_name:" + token);
    }
    public void deleteAdminToken(String username) {
        redisTemplate.delete("admin:" + username);
    }
    public void deleteAdminSession(String token) {
        redisTemplate.delete("Admin:" + token);
        redisTemplate.delete("AdminRole:" + token);
    }

}
