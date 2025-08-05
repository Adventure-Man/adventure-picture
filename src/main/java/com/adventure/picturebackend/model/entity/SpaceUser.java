package com.adventure.picturebackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间用户关联 实体类。
 *
 * @author Administrator
 * @since 2025-08-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("space_user")
public class SpaceUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 空间 id
     */
    @Column("spaceId")
    private Long spaceId;

    /**
     * 用户 id
     */
    @Column("userId")
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    @Column("spaceRole")
    private String spaceRole;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

}
