package com.linghang.backend.mywust_basic.Utils;

import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.ImageModerationRequest;
import com.aliyun.green20220302.models.ImageModerationResponse;
import com.aliyun.green20220302.models.ImageModerationResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import cn.wustlinghang.mywust.common.oss.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AliyunReviewTemplate {

    @Autowired
    private OssProperties ossProperties;

    /**
     * 审核图片内容
     * @param imageUrl 图片的公共访问URL
     * @return 风险等级 (0: 安全, 1: 存疑, 2: 违规)
     */
    public Map<String, Object> reviewImage(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel", 0);
        result.put("reason", "检测通过");

        try {
            Config config = new Config()
                    .setAccessKeyId(ossProperties.getAccessKeyId())
                    .setAccessKeySecret(ossProperties.getAccessKeySecret())
                    .setEndpoint("green-cip.cn-beijing.aliyuncs.com"); // 统一接入点

            Client client = new Client(config);

            // 构造请求
            Map<String, String> serviceParameters = new HashMap<>();
            serviceParameters.put("imageUrl", imageUrl);
            
            ImageModerationRequest request = new ImageModerationRequest()
                    .setService("baselineCheck") // 基础策略
                    .setServiceParameters(com.aliyun.teautil.Common.toJSONString(serviceParameters));

            RuntimeOptions runtime = new RuntimeOptions();
            ImageModerationResponse response = client.imageModerationWithOptions(request, runtime);
            
            if (response.getStatusCode() == 200) {
                ImageModerationResponseBody body = response.getBody();
                ImageModerationResponseBody.ImageModerationResponseBodyData data = body.getData();
                
                // 结果解析
                String label = data.getResult().get(0).getLabel();
                if (!"nonLabel".equals(label)) {
                    log.warn("图片机审异常, 标签: {}, URL: {}", label, imageUrl);
                    result.put("riskLevel", 1);
                    result.put("reason", "机审标签: " + label);
                }
            }
        } catch (Exception e) {
            log.error("阿里云图片审核服务调用失败", e);
            result.put("riskLevel", 1);
            result.put("reason", "审核服务暂时不可用: " + e.getMessage());
        }
        return result;
    }
}
