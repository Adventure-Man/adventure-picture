package com.adventure.picturebackend.model.dto.picture;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 管理员-空间修改请求
 */
@Data
public class SpaceUpdateRequest {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 当前空间下的图片数量
     */
    private Long maxCount;
}
