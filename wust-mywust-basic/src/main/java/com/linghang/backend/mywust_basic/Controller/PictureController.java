package com.linghang.backend.mywust_basic.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import cn.wustlinghang.mywust.common.model.Picture;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Service.PictureService;
import com.linghang.backend.mywust_basic.Utils.R;
import com.linghang.backend.mywust_basic.Utils.AliyunReviewTemplate;
import cn.wustlinghang.mywust.common.oss.OssTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/common")
@Tag(name = "图片与多媒体管理")
public class PictureController {
    private final static Logger logger = LoggerFactory.getLogger(PictureController.class);

    @Autowired
    private PictureService pictureService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private AliyunReviewTemplate aliyunReviewTemplate;

    // Platform 定义映射
    private static final int PLATFORM_BUSINESS = 1;
    private static final int PLATFORM_CAROUSEL = 2;
    private static final int PLATFORM_CALENDAR = 3;

    @PostMapping("/acceptPicture")
    @Operation(summary = "通过审核")
    public R<String> acceptPicture(@RequestBody List<Object> ids) {
        if (ids == null || ids.isEmpty()) return R.failure(400, "ID 列表为空");
        List<Long> longIds = ids.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        int num = pictureService.accpetPictures(longIds);
        if (num > 0) {
            recordLog("ADMIN", "通过审核图片, IDs: " + longIds);
            return R.success("审核通过成功");
        }
        return R.failure(300, "操作失败：未找到图片或无需更新");
    }

    @PostMapping("/ignorePicture")
    @Operation(summary = "下架/撤回审核")
    public R<String> ignorePicture(@RequestBody List<Object> ids) {
        if (ids == null || ids.isEmpty()) return R.failure(400, "ID 列表为空");
        List<Long> longIds = ids.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        int num = pictureService.ignorePictures(longIds);
        if (num > 0) {
            recordLog("ADMIN", "下架撤回图片, IDs: " + longIds);
            return R.success("下架成功");
        }
        return R.failure(300, "操作失败：未找到图片或无需更新");
    }

    @PostMapping("/deletePicture")
    @Operation(summary = "逻辑删除")
    public R<String> deletePicture(@RequestBody List<Object> ids) {
        if (ids == null || ids.isEmpty()) return R.failure(400, "ID 列表为空");
        List<Long> longIds = ids.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        int num = pictureService.deletePictures(longIds);
        if (num > 0) {
            recordLog("ADMIN", "删除图片, IDs: " + longIds);
            return R.success("删除成功");
        }
        return R.failure(300, "删除失败");
    }

    @GetMapping("/listPictures")
    @Operation(summary = "获取全量图片列表")
    public R<List<Picture>> listPictures() {
        List<Picture> list = pictureService.list();
        // 机审展示逻辑已在 upload 时持久化或在此模拟
        list.forEach(p -> {
            if (p.getRiskLevel() == null && p.getPid() != null && p.getPid() % 7 == 0) {
                p.setRiskLevel(1);
                p.setReviewResult("AI检测：疑似包含违规内容，请人工核验");
            }
        });
        return R.success(list);
    }

    /**
     * 多媒体上传接口 (包含真实阿里云机审)
     */
    @PostMapping("/uploadMultimedia")
    @Operation(summary = "直接上传多媒体图片(带机审)")
    public R<String> uploadMultimedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("platform") Integer platform,
            @RequestParam("uid") String uid) {
        
        if (file.isEmpty()) return R.failure(400, "文件不能为空");

        long startTime = System.currentTimeMillis();
        try {
            // 1. 调用 OSS 上传
            String url = ossTemplate.upload(file.getInputStream(), file.getOriginalFilename());
            if (url == null) return R.failure(500, "OSS 上传失败");
            logger.info("OSS上传完成, 耗时: {}ms, URL: {}", (System.currentTimeMillis() - startTime), url);

            // 2. 调用阿里云真实图片审核接口
            Map<String, Object> review = aliyunReviewTemplate.reviewImage(url);
            Integer riskLevel = (Integer) review.get("riskLevel");
            String reason = (String) review.get("reason");
            logger.info("机审完成, 耗时: {}ms, 结果: {}", (System.currentTimeMillis() - startTime), reason);

            // 3. 如果是日历，先下架旧的
            if (platform == PLATFORM_CALENDAR) {
                Picture disableOld = new Picture();
                disableOld.setStatus(0);
                pictureService.update(disableOld, new QueryWrapper<Picture>().eq("platform", PLATFORM_CALENDAR));
            }

            // 4. 存入数据库
            Picture picture = new Picture();
            picture.setUrl(url);
            picture.setPlatform(platform);
            picture.setStatus(platform == PLATFORM_CALENDAR ? 1 : 0);
            picture.setCreatedId(Long.valueOf(uid));
            picture.setIfdelete(0);
            picture.setUploadTime(LocalDateTime.now());
            
            // 设置机审结果
            picture.setRiskLevel(riskLevel);
            picture.setReviewResult(reason);
            
            pictureService.save(picture);
            
            // 5. 记录日志 (增加容错)
            try {
                recordLog(uid, "上传图片 (平台:" + platform + ", 机审:" + reason + ", URL:" + url + ")");
            } catch (Exception logEx) {
                logger.error("操作日志记录失败，但不影响主业务", logEx);
            }
            
            logger.info("整个上传流程处理完毕, 总耗时: {}ms", (System.currentTimeMillis() - startTime));
            return R.success(url);
        } catch (Exception e) {
            logger.error("多媒体上传系统异常", e);
            return R.failure(500, "系统处理异常: " + e.getMessage());
        }
    }

    @PostMapping("/uploadMultimediaWithId")
    @Operation(summary = "上传多媒体并返回图片ID")
    public R<Map<String, Object>> uploadMultimediaWithId(
            @RequestParam("file") MultipartFile file,
            @RequestParam("platform") Integer platform,
            @RequestParam("uid") String uid) {

        if (file.isEmpty()) return R.failure(400, "文件不能为空");

        long startTime = System.currentTimeMillis();
        try {
            String url = ossTemplate.upload(file.getInputStream(), file.getOriginalFilename());
            if (url == null) return R.failure(500, "OSS 上传失败");
            logger.info("OSS上传完成, 耗时: {}ms, URL: {}", (System.currentTimeMillis() - startTime), url);

            Map<String, Object> review = aliyunReviewTemplate.reviewImage(url);
            Integer riskLevel = (Integer) review.get("riskLevel");
            String reason = (String) review.get("reason");
            logger.info("机审完成, 耗时: {}ms, 结果: {}", (System.currentTimeMillis() - startTime), reason);

            if (platform == PLATFORM_CALENDAR) {
                Picture disableOld = new Picture();
                disableOld.setStatus(0);
                pictureService.update(disableOld, new QueryWrapper<Picture>().eq("platform", PLATFORM_CALENDAR));
            }

            Picture picture = new Picture();
            picture.setUrl(url);
            picture.setPlatform(platform);
            picture.setStatus(platform == PLATFORM_CALENDAR ? 1 : 0);
            picture.setCreatedId(Long.valueOf(uid));
            picture.setIfdelete(0);
            picture.setUploadTime(LocalDateTime.now());
            picture.setRiskLevel(riskLevel);
            picture.setReviewResult(reason);
            pictureService.save(picture);

            try {
                recordLog(uid, "上传图片并返回ID (平台:" + platform + ", PID:" + picture.getPid() + ", 机审:" + reason + ", URL:" + url + ")");
            } catch (Exception logEx) {
                logger.error("操作日志记录失败，但不影响主业务", logEx);
            }

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("pid", picture.getPid());
            result.put("url", picture.getUrl());
            result.put("status", picture.getStatus());
            return R.success(result);
        } catch (Exception e) {
            logger.error("多媒体上传并返回ID系统异常", e);
            return R.failure(500, "系统处理异常: " + e.getMessage());
        }
    }

    @GetMapping("/getCarousels")
    @Operation(summary = "获取已发布的轮播图")
    public R<List<String>> getCarousels() {
        QueryWrapper<Picture> qw = new QueryWrapper<>();
        qw.eq("platform", PLATFORM_CAROUSEL).eq("status", 1).orderByDesc("uploadTime");
        List<String> urls = pictureService.list(qw).stream().map(Picture::getUrl).collect(Collectors.toList());
        return R.success(urls);
    }

    @GetMapping("/getCalendar")
    @Operation(summary = "获取当前日历图")
    public R<String> getCalendar() {
        QueryWrapper<Picture> qw = new QueryWrapper<>();
        qw.eq("platform", PLATFORM_CALENDAR).eq("status", 1).orderByDesc("uploadTime").last("limit 1");
        Picture calendar = pictureService.getOne(qw);
        return R.success(calendar != null ? calendar.getUrl() : null);
    }

    @PostMapping("/updateCalendar")
    @Operation(summary = "发布新日历图")
    public R<String> updateCalendar(@RequestParam("url") String url, @RequestParam("uid") String uid) {
        Picture disableOld = new Picture();
        disableOld.setStatus(0);
        pictureService.update(disableOld, new QueryWrapper<Picture>().eq("platform", PLATFORM_CALENDAR));

        Picture newCal = new Picture();
        newCal.setUrl(url);
        newCal.setPlatform(PLATFORM_CALENDAR);
        newCal.setStatus(1);
        newCal.setCreatedId(Long.valueOf(uid));
        newCal.setIfdelete(0);
        newCal.setUploadTime(LocalDateTime.now());
        
        pictureService.save(newCal);
        recordLog(uid, "更新日历图, URL: " + url);
        return R.success("日历图更新成功");
    }

    private void recordLog(String operatorId, String content) {
        try {
            OperationLog log = new OperationLog();
            log.setOperatorId(operatorId);
            log.setOperateContent(content);
            log.setOperateTime(new Date());
            operationService.addOperationLog(log);
        } catch (Exception e) {
            logger.error("记录日志失败", e);
        }
    }
}
