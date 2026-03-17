package com.linghang.backend.mywust_basic.Service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wustlinghang.mywust.common.model.Picture;
import com.linghang.backend.mywust_basic.Mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PictureService extends ServiceImpl<PictureMapper, Picture> {

    @Transactional
    public int accpetPictures(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        Picture picture = new Picture();
        picture.setStatus(1);
        UpdateWrapper<Picture> wrapper = new UpdateWrapper<>();
        wrapper.in("pid", ids); // 修正：主键名为 pid
        return this.baseMapper.update(picture, wrapper) >= 0 ? ids.size() : 0;
    }

    @Transactional
    public int ignorePictures(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        Picture picture = new Picture();
        picture.setStatus(0);
        UpdateWrapper<Picture> wrapper = new UpdateWrapper<>();
        wrapper.in("pid", ids); // 修正：主键名为 pid
        return this.baseMapper.update(picture, wrapper) >= 0 ? ids.size() : 0;
    }

    @Transactional
    public int deletePictures(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        // removeByIds 内部会自动适配 @TableId
        return this.removeByIds(ids) ? ids.size() : 0;
    }

    public Picture getPicture(Long id) {
        return this.getById(id);
    }
}
