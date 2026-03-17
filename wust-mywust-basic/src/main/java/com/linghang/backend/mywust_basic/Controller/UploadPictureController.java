package com.linghang.backend.mywust_basic.Controller;

import cn.wustlinghang.mywust.common.model.Picture;
import com.linghang.backend.mywust_basic.Service.PictureService;
import com.linghang.backend.mywust_basic.Utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "图片上传报备", description = "供上传插件或业务模块调用")
@RestController
@RequestMapping("/admin/common")
public class UploadPictureController {

    @Autowired
    private PictureService pictureService;

    @Operation(summary = "报备新图片", description = "上传OSS后将URL记录到数据库")
    @PostMapping("/addPicture")
    public R<Integer> addPicture(@RequestParam("url") String url, @RequestParam("uid") String uid) {
        Picture picture = new Picture();
        picture.setUrl(url);
        picture.setCreatedId(Long.valueOf(uid));
        picture.setUploadTime(LocalDateTime.now());
        picture.setStatus(0); // 默认待审核
        
        // 修复：使用 MyBatis-Plus 的 save 方法
        boolean success = pictureService.save(picture);
        return success ? R.success(1) : R.failure(500, "保存失败");
    }
}
