package com.adventure.picturebackend.model.dto.sapce;

import com.adventure.picturebackend.common.req.PageRequest;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.enums.SpaceLevelEnum;
import com.adventure.picturebackend.model.vo.UserVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 空间查询请求
 */
@Data
public class SpaceQueryRequest extends PageRequest {
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

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;


    /**
     * 空间转空间查询
     */
    public static SpaceQueryRequest toSpaceQueryRequest(Space space) {
        SpaceQueryRequest spaceQueryRequest = new SpaceQueryRequest();
        spaceQueryRequest.setId(space.getId());
        spaceQueryRequest.setSpaceName(space.getSpaceName());
        spaceQueryRequest.setSpaceLevel(space.getSpaceLevel().getValue());
        spaceQueryRequest.setMaxSize(space.getMaxSize());
        spaceQueryRequest.setMaxCount(space.getMaxCount());
        spaceQueryRequest.setUserId(space.getUserId());
        spaceQueryRequest.setCreateTime(space.getCreateTime());
        spaceQueryRequest.setEditTime(space.getEditTime());
        spaceQueryRequest.setUpdateTime(space.getUpdateTime());
        return spaceQueryRequest;

    }
    /**
     * 空间查询转为空间
     */
    public Space spaceQueryRequest2Space(SpaceQueryRequest spaceQueryRequest) {
        Space space = new Space();
        space.setId(spaceQueryRequest.getId());
        space.setSpaceName(spaceQueryRequest.getSpaceName());
        space.setSpaceLevel(SpaceLevelEnum.getEnumByValue(spaceQueryRequest.getSpaceLevel()));
        space.setMaxSize(spaceQueryRequest.getMaxSize());
        space.setMaxCount(spaceQueryRequest.getMaxCount());
        space.setUserId(spaceQueryRequest.getUserId());
        space.setCreateTime(spaceQueryRequest.getCreateTime());
        space.setEditTime(spaceQueryRequest.getEditTime());
        space.setUpdateTime(spaceQueryRequest.getUpdateTime());
        return space;
    }
}
