package com.adventure.picturebackend.service;

import com.adventure.picturebackend.model.dto.sapce.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceQueryRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceUpdateRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.space.SpaceVO;
import com.adventure.picturebackend.model.vo.spaceuser.SpaceUserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.adventure.picturebackend.model.entity.SpaceUser;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 空间用户关联 服务层。
 *
 * @author Administrator
 * @since 2025-08-05
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 管理员-修改用户空间
     * @param spaceUserEditRequest
     * @return
     */
    Boolean updateSpaceUser(SpaceUserEditRequest spaceUserEditRequest);
    /**
     * 保存空间用户
     * @param spaceUserAddRequest
     * @return
     */
    Long saveSpaceUser(SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request);

    /**
     * 校验空间用户权限
     *
     * @param spaceUser
     * @param
     */
    void checkSpaceUserAuth(Boolean add, SpaceUser spaceUser);

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间信息VO
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间信息VO列表
     * @param spacePage
     * @return
     */
    List<SpaceUserVO> getSpaceUserVoList(List<SpaceUser> spacePage);

}
