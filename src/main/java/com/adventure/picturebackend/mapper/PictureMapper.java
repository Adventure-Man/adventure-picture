package com.adventure.picturebackend.mapper;

import com.adventure.picturebackend.model.entity.Picture;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图片 映射层。
 *
 * @author Administrator
 * @since 2025-07-14
 */
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {

}
