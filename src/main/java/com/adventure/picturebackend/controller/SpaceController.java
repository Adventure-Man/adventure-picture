package com.adventure.picturebackend.controller;

import com.adventure.picturebackend.aop.annotation.AuthCheck;
import com.adventure.picturebackend.common.constant.UserConstant;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.IpHelper;
import com.adventure.picturebackend.common.utils.ResultUtils;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.config.SpaceCapacityConfig;
import com.adventure.picturebackend.model.dto.picture.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.picture.SpaceUpdateRequest;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.SpaceLevelEnum;
import com.adventure.picturebackend.model.vo.SpaceLevelVO;
import com.adventure.picturebackend.service.SpaceService;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.adventure.picturebackend.model.entity.Space;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevelVO>> listSpaceLevel() {
        List<SpaceLevelVO> spaceLevelList = spaceService.getSpaceLevels();
        String ipAddr = IpHelper.getIpAddr();
        log.info("ip: {}",ipAddr);
        return ResultUtils.success(spaceLevelList);
    }


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
     * 根据主键删除。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return spaceService.removeById(id);
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
}
