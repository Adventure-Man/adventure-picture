package com.adventure.picturebackend.model.dto.spaceuser;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Adventure
 * @date 2025/8/5
 * @description 空间编辑请求
 */
@Data
public class SpaceUserEditRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
