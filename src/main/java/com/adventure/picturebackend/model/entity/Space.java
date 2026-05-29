package com.adventure.picturebackend.model.entity;

import com.adventure.picturebackend.model.enums.SpaceLevelEnum;
import com.adventure.picturebackend.model.enums.SpaceTypeEnum;
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
 *  实体类。
 *
 * @author Administrator
 * @since 2025-07-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("space")
public class Space implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 空间id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 空间名称
     */
    @Column("spaceName")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Column("spaceLevel")
    private SpaceLevelEnum spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @Column("maxSize")
    private Long maxSize;

    /**
     * 当前空间下的图片数量
     */
    @Column("maxCount")
    private Long maxCount;

    /**
     * 当前空间的总大小
     */
    @Column("totalSize")
    private Long totalSize;

    /**
     * 当前空间图片的总数量
     */
    @Column("totalCount")
    private Long totalCount;

    /**
     * 创建用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column("isDelete")
    private Integer isDelete;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    @Column("spaceType")
    private SpaceTypeEnum spaceType;

}
