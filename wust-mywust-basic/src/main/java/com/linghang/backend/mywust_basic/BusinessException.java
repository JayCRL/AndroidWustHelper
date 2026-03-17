package com.linghang.backend.mywust_basic;

/**
 * 业务核验失败异常（如图片不存在、密码不匹配）
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}