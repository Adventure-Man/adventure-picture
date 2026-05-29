package com.adventure.picturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.req.DeleteRequest;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.exception.ErrorCode;
import com.adventure.picturebackend.common.utils.ResultUtils;
import com.adventure.picturebackend.common.exception.ThrowUtils;
import com.adventure.picturebackend.manager.auth.SpaceUserPermissionConstant;
import com.adventure.picturebackend.manager.auth.anno.SaSpaceCheckPermission;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.adventure.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.spaceuser.SpaceUserVO;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.adventure.picturebackend.model.entity.SpaceUser;
import com.adventure.picturebackend.service.SpaceUserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 空间用户关联 控制层。
 *
 * @author Administrator
 * @since 2025-08-05
 */
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Autowired
    private SpaceUserService spaceUserService;

    @Autowired
    private UserService userService;

    /**
     * 添加空间用户关联。
     *
     * @param spaceUserAddRequest 空间用户关联
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> save(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        return ResultUtils.success(spaceUserService.saveSpaceUser(spaceUserAddRequest, request));
    }

    /**
     * 根据主键删除空间用户关联。
     *
     * @param deleteRequest 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> remove(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        SpaceUser byId = spaceUserService.getById(id);
        // 校验用户是否在该空间内，如果不是则返回错误
        Long userId = byId.getUserId();
        List<SpaceUser> list = spaceUserService.list(new QueryWrapper().eq(SpaceUser::getUserId, userId));
        boolean contains = list.contains(byId);
        ThrowUtils.throwIf(!contains, ErrorCode.PARAMS_ERROR);
        boolean b = spaceUserService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据主键更新空间用户关联。
     *
     * @param spaceUserEditRequest 空间用户关联
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> update(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceUserService.updateSpaceUser(spaceUserEditRequest));
    }

    /**
     * 查询所有空间用户关联。
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> list(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> list = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(list));
    }

    /**
     * 根据空间用户关联主键获取详细信息。
     *
     * @param spaceUserQueryRequest 空间用户关联主键
     * @return 空间用户关联详情
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getInfo(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId,userId), ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = spaceUserService.getQueryWrapper(spaceUserQueryRequest);
        SpaceUser one = spaceUserService.getOne(queryWrapper);
        ThrowUtils.throwIf(one == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(one);
    }

    /**
     * 获取用户的团队空间 一个用户可加入多个团队空间
     * @param request
     * @return
     */
    @PostMapping("/list/myTeamSpace")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(id);
        List<SpaceUser> list = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(list));
    }


}
