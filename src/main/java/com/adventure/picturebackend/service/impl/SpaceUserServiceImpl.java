package com.adventure.picturebackend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.SpaceRoleEnum;
import com.adventure.picturebackend.model.vo.space.SpaceVO;
import com.adventure.picturebackend.model.vo.spaceuser.SpaceUserVO;
import com.adventure.picturebackend.service.SpaceService;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.adventure.picturebackend.model.entity.SpaceUser;
import com.adventure.picturebackend.mapper.SpaceUserMapper;
import com.adventure.picturebackend.service.SpaceUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空间用户关联 服务层实现。
 *
 * @author Administrator
 * @since 2025-08-05
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Autowired
    private UserService userService;
    @Autowired
    private SpaceService spaceService;

    @Override
    public Boolean updateSpaceUser(SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = spaceUserEditRequest.getId();
        String spaceRole = spaceUserEditRequest.getSpaceRole();
        ThrowUtils.throwIf(spaceRole == null, ErrorCode.PARAMS_ERROR);
        SpaceUser oldSpaceUser = this.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceUser spaceUser = SpaceUser.builder().id(id).spaceRole(spaceRole).build();
        this.checkSpaceUserAuth(false, spaceUser);
        return this.updateById(spaceUser);
    }

    @Override
    public Long saveSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        if (spaceUserAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1.校验空间和用户是否存在
        // 2.校验用户是否已经在该空间内
        SpaceUser spaceUser = SpaceUser.builder().spaceId(spaceUserAddRequest.getSpaceId()).userId(spaceUserAddRequest.getUserId()).spaceRole(spaceUserAddRequest.getSpaceRole()).build();
        this.checkSpaceUserAuth(true, spaceUser);
        boolean save = this.save(spaceUser);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存失败");
        }
        return spaceUser.getId();
    }

    @Override
    public void checkSpaceUserAuth(Boolean add, SpaceUser spaceUser) {
        Long spaceId = spaceUser.getSpaceId();
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        // 1.判断用户和空间是否存在
        if (add) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            User byId = userService.getById(spaceUser.getUserId());
            ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 2.判断用户是否是空间管理员
        if (!SpaceRoleEnum.ADMIN.equals(spaceRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId, spaceRole), ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SpaceUser::getId, id, ObjectUtil.isNotEmpty(id));
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId, ObjectUtil.isNotEmpty(spaceId));
        queryWrapper.eq(SpaceUser::getUserId, userId, ObjectUtil.isNotEmpty(userId));
        queryWrapper.eq(SpaceUser::getSpaceRole, spaceRole, ObjectUtil.isNotEmpty(spaceRole));
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        Long userId = spaceUserVO.getUserId();
        Long spaceId = spaceUserVO.getSpaceId();
        if (userId != null) {
            User user = userService.getById(userId);
            spaceUserVO.setUser(userService.getUserVO(user));
        }
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVoList(List<SpaceUser> spacePage) {
        List<SpaceUserVO> spaceUserVOList = spacePage.stream().map(SpaceUserVO::objToVo).toList();
        // 获取所有用户id
        List<Long> userIds = spaceUserVOList.stream().map(SpaceUserVO::getUserId).toList();
        // 获取spaceId
        List<Long> spaceIds = spaceUserVOList.stream().map(SpaceUserVO::getSpaceId).toList();
        if (!userIds.isEmpty()) {
            Map<Long, List<User>> collect = userIds.stream().map(userService::getById).collect(Collectors.groupingBy(User::getId));
            spaceUserVOList.forEach(spaceUserVO -> {
                Long userId = spaceUserVO.getUserId();
                if (collect.containsKey(userId)) {
                    spaceUserVO.setUser(userService.getUserVO(collect.get(userId).get(0)));
                }
            });
        }
        if (!spaceIds.isEmpty()) {
            Map<Long, List<Space>> collect = spaceIds.stream().map(spaceService::getById).collect(Collectors.groupingBy(Space::getId));
            spaceUserVOList.forEach(spaceUserVO -> {
                Long spaceId = spaceUserVO.getSpaceId();
                if (collect.containsKey(spaceId)) {
                    spaceUserVO.setSpace(SpaceVO.objToVo(collect.get(spaceId).get(0)));
                }
            });
        }
        return spaceUserVOList;
    }
}
