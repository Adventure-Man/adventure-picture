package com.adventure.picturebackend.model.dto.picture;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 普通用户-空间修改请求
 */
@Data
public class SpaceEditRequest {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

}
