package com.adventure.picturebackend.controller;

import com.adventure.picturebackend.aop.annotation.AuthCheck;
import com.adventure.picturebackend.common.constant.UserConstant;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.exception.ErrorCode;
import com.adventure.picturebackend.common.utils.IpHelperUtils;
import com.adventure.picturebackend.common.utils.ResultUtils;
import com.adventure.picturebackend.common.exception.ThrowUtils;
import com.adventure.picturebackend.config.SpaceCapacityConfig;
import com.adventure.picturebackend.manager.auth.SpaceUserAuthManager;
import com.adventure.picturebackend.model.dto.sapce.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceQueryRequest;
import com.adventure.picturebackend.model.dto.sapce.SpaceUpdateRequest;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.adventure.picturebackend.model.vo.space.SpaceLevelVO;
import com.adventure.picturebackend.model.vo.space.SpaceVO;
import com.adventure.picturebackend.service.SpaceService;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *  控制层。
 *
 * @author Administrator
 * @since 2025-07-25
 */
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private UserService userService;
    @Autowired
    private SpaceCapacityConfig spaceCapacityConfig;

    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;




    /**
     * 添加。用户创建私有空间
     *
     * @param spaceAddRequest
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    public BaseResponse<Long > saveSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 校验
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceService.saveSpace(spaceAddRequest,loginUser));
    }

    /**
     * 根据主键更新空间。
     *
     * @param spaceUpdateRequest
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = spaceService.updateSpace(spaceUpdateRequest);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取空间列表。
     *
     * @param spaceQueryRequest
     * @return 空间列表
     */
    @PostMapping("list/page")
    public BaseResponse<Page<SpaceVO>> listSpacePage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Space> picturePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest, request));
        Page<SpaceVO> spaceVOPage = spaceService.getSpaceVoList(picturePage, request);
        return ResultUtils.success(spaceVOPage);

    }

    /**
     * 根据空间主键获取详细信息（脱敏）。
     *
     * @param id 图片主键
     * @return 图片详情
     */
    @GetMapping("/{id}")
    public BaseResponse<SpaceVO> getSpaceVOById(@PathVariable Long id, HttpServletRequest request) {
        // 参数校验
        if (id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        // 查询数据库
        Space space = spaceService.getById(id);
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        ThrowUtils.throwIf(spaceVO == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验 只空间管理员查看
//        Long spaceId = spaceVO.getSpaceId();
//        if (spaceId != null) {
//            spaceService.checkPictureAuth(loginUser, picture);
//        }
//        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevelVO>> listSpaceLevel() {
        List<SpaceLevelVO> spaceLevelList = spaceService.getSpaceLevels();
        String ipAddr = IpHelperUtils.getIpAddr();
        log.info("ip: {}",ipAddr);
        return ResultUtils.success(spaceLevelList);
    }
}
