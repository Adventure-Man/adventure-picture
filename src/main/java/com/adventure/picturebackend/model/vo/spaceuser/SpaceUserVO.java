package com.adventure.picturebackend.model.vo.spaceuser;

import com.adventure.picturebackend.model.entity.SpaceUser;
import com.adventure.picturebackend.model.vo.UserVO;
import com.adventure.picturebackend.model.vo.space.SpaceVO;
import com.mybatisflex.annotation.Column;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * @author Adventure
 * @date 2025/8/5
 * @description 空间用户VO
 */
@Data
public class SpaceUserVO {
    /**
     * id
     */
    private Long id;

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

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户VO
     */
    private UserVO user;

    /**
     * 空间VO
     */
    private SpaceVO space;

    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }

    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }
}
