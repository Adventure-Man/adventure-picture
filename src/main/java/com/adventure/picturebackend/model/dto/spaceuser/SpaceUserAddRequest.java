package com.adventure.picturebackend.model.dto.spaceuser;

/**
 * @author Adventure
 * @date 2025/8/5
 * @description 空间用户添加请求
 */

import lombok.Data;

@Data
public class SpaceUserAddRequest {
    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
