package com.adventure.picturebackend.model.dto.picture;

import cn.hutool.json.JSONUtil;
import com.adventure.picturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签  ---> 数据库存储为JSON格式
     */
    private List<String> tags;

    /**
     * 空间id
     */
    private Long spaceId;


    public static Picture dtoToObj(PictureUpdateRequest pictureUpdateRequest) {
        if (pictureUpdateRequest == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        return picture;
    }

    private static final long serialVersionUID = 1L;
}
