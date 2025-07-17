package com.adventure.picturebackend.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @author Adventure
 * @date 2025/7/15
 * @description 图片分类和标签列表
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;
    /**
     * 分类列表
     */
    private List<String> categoryList;
}
