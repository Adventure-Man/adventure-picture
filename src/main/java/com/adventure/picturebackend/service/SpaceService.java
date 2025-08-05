package com.adventure.picturebackend.service;

import com.adventure.picturebackend.model.dto.sapce.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceQueryRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceUpdateRequest;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.space.SpaceLevelVO;
import com.adventure.picturebackend.model.vo.space.SpaceVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SpaceService extends IService<Space> {
    /**
     * 管理员-修改空间
     * @param spaceUpdateRequest
     * @return
     */
    Boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest);
    /**
     * 保存空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    Long saveSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 更新空间额度信息
     * @param spaceId
     * @return
     */
    boolean updateSpaceQuota(Long spaceId, Long picSize, int i, boolean isDelete);

    /**
     * 返回空间信息
     */
    List<SpaceLevelVO> getSpaceLevels();

    /**
     * 校验空间权限
     *
     * @param space
     * @param
     */
    void checkSpaceAuth(User loginUser, Space space);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    QueryWrapper getQueryWrapper(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request);

    /**
     * 获取空间信息VO
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间信息VO列表
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVoList(Page<Space> spacePage, HttpServletRequest request);
}
