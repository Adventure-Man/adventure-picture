package com.adventure.picturebackend.service;

import com.adventure.picturebackend.model.dto.picture.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.picture.SpaceUpdateRequest;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.SpaceLevelVO;
import com.mybatisflex.core.service.IService;

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
}
