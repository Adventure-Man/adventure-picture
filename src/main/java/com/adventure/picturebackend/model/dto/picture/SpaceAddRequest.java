package com.adventure.picturebackend.model.dto.picture;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 空间添加请求
 */
@Data
public class SpaceAddRequest {
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;
}
