package com.adventure.picturebackend.model.dto.sapce;

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

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;

}
