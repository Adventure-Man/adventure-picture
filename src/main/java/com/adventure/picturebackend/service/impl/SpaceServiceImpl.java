package com.adventure.picturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.config.SpaceCapacityConfig;
import com.adventure.picturebackend.model.dto.picture.SpaceAddRequest;
import com.adventure.picturebackend.model.dto.picture.SpaceUpdateRequest;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.SpaceLevelEnum;
import com.adventure.picturebackend.model.vo.SpaceLevelVO;
import com.adventure.picturebackend.service.SpaceService;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.mapper.SpaceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  服务层实现。
 *
 * @author Administrator
 * @since 2025-07-25
 */
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>  implements SpaceService {
    @Autowired
    private SpaceCapacityConfig spaceCapacityConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;
    // ConcurrentHashMap存储本地锁
    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public Boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest) {
        // 参数校验
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 业务逻辑
        Space spaceBuilder = Space.builder()
                .id(spaceUpdateRequest.getId())
                .spaceName(spaceUpdateRequest.getSpaceName())
                .spaceLevel(SpaceLevelEnum.getEnumByValue(spaceUpdateRequest.getSpaceLevel()))
                .maxSize(spaceUpdateRequest.getMaxSize())
                .maxCount(spaceUpdateRequest.getMaxCount())
                .build();
        // 填充空间信息
        fillSpaceBySpaceLevel(spaceBuilder);
        // 数据校验
        validateSpace(spaceBuilder, false);
        Space byId = this.getById(spaceBuilder.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        // 数据返回
        boolean flag = this.updateById(spaceBuilder);
        if (!flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    public Long saveSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 参数校验
        if (spaceAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 业务逻辑
        Space spaceBuilder = Space.builder()
                .spaceName(spaceAddRequest.getSpaceName())
                .spaceLevel(SpaceLevelEnum.getEnumByValue(spaceAddRequest.getSpaceLevel()))
                .build();
        if(spaceAddRequest.getSpaceName().isBlank()){
            spaceBuilder.setSpaceName("默认空间");
        }
        if (spaceBuilder.getSpaceLevel() == null){
            spaceBuilder.setSpaceLevel(SpaceLevelEnum.ORDINARY);
        }
        // 填充空间信息
        fillSpaceBySpaceLevel(spaceBuilder);
        // 数据校验
        validateSpace(spaceBuilder, true);
        Long userId = loginUser.getId();
        spaceBuilder.setUserId(userId);
        // 非管理员只能创建普通级别的空间
        if (!userService.isAdmin(loginUser) && spaceBuilder.getSpaceLevel() != SpaceLevelEnum.ORDINARY){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限创建指定级别的空间");
        }
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
        synchronized (lock) {
            try {
                // 数据库操作
                Long newSpaceId = transactionTemplate.execute(status -> {
                    long count = this.count(new QueryWrapper().eq(Space::getUserId, userId));
                    ThrowUtils.throwIf(count >= 1, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
                    // 写入数据库
                    boolean result = this.save(spaceBuilder);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                    // 返回新写入的数据 id
                    return spaceBuilder.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                // 防止内存泄漏
                lockMap.remove(userId);
            }
        }
    }

    @Override
    public boolean updateSpaceQuota(Long spaceId, Long picSize, int i, boolean isDelete) {
        // 更新空间额度信息
        Space space = this.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 判断是否是删除
        if (isDelete) {
            space.setTotalSize(space.getTotalSize() - picSize * i);
            space.setTotalCount(space.getTotalCount() - i);
        }else {
            space.setTotalSize(space.getTotalSize() + picSize * i);
            space.setTotalCount(space.getTotalCount() + i);
        }
        return this.updateById(space);

    }

    @Override
    public List<SpaceLevelVO> getSpaceLevels() {
        List<SpaceLevelVO> spaceLevels = new ArrayList<>();
        // 添加默认级别
        spaceLevels.add(new SpaceLevelVO(-1,"default", spaceCapacityConfig.getMaxCount(), spaceCapacityConfig.getMaxSize()));
        // 添加默认级别
        for (SpaceLevelEnum value : SpaceLevelEnum.values()) {
            String name = value.name();
            log.info("level name: {}",name);
            if ("ORDINARY".equalsIgnoreCase(name)){
                spaceLevels.add(new SpaceLevelVO(value.getValue(), value.getText(),
                        spaceCapacityConfig.getOrdinary().getMaxCount(), spaceCapacityConfig.getOrdinary().getMaxSize()));
            };
            if ("PROFESSIONAL".equalsIgnoreCase(name)){
                spaceLevels.add(new SpaceLevelVO(value.getValue(), value.getText(),
                        spaceCapacityConfig.getProfessional().getMaxCount(), spaceCapacityConfig.getProfessional().getMaxSize()));
            };
            if ("FLAGSHIP".equalsIgnoreCase(name)){
                spaceLevels.add(new SpaceLevelVO(value.getValue(), value.getText(),
                        spaceCapacityConfig.getFlagship().getMaxCount(), spaceCapacityConfig.getFlagship().getMaxSize()));
            }
        }
        // 添加其他级别
        return spaceLevels;
    }

    public void validateSpace(Space spaceBuilder, boolean add) {
        ThrowUtils.throwIf(spaceBuilder == null, ErrorCode.PARAMS_ERROR);
        // 取值
        Integer spaceLevel = spaceBuilder.getSpaceLevel().getValue();
        String spaceName = spaceBuilder.getSpaceName();
        Long spaceId = spaceBuilder.getId();
        ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        // 添加时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称过长");
            ThrowUtils.throwIf(spaceLevel < 0 || spaceLevel > 3, ErrorCode.PARAMS_ERROR, "空间等级错误");
        }
        // 修改时，参数不能为空
        if (!add) {
            ThrowUtils.throwIf(spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称过长");
            ThrowUtils.throwIf(spaceLevel < 0 || spaceLevel > 3, ErrorCode.PARAMS_ERROR, "空间等级错误");
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间id不能为空");
        }
    }

    public void fillSpaceBySpaceLevel(Space space){
        Long maxCount;
        Long maxSize;
        // 根据空间等级设置空间大小和数量
        switch (space.getSpaceLevel()) {
            case ORDINARY:
                maxCount = spaceCapacityConfig.getOrdinary().getMaxCount();
                maxSize = spaceCapacityConfig.getOrdinary().getMaxSize();
                space.setMaxCount(maxCount);
                space.setMaxSize(maxSize);
                break;
            case PROFESSIONAL:
                maxCount = spaceCapacityConfig.getProfessional().getMaxCount();
                maxSize = spaceCapacityConfig.getProfessional().getMaxSize();
                space.setMaxCount(maxCount);
                space.setMaxSize(maxSize);
                break;
            case FLAGSHIP:
                maxCount = spaceCapacityConfig.getFlagship().getMaxCount();
                maxSize = spaceCapacityConfig.getFlagship().getMaxSize();
                space.setMaxCount(maxCount);
                space.setMaxSize(maxSize);
                break;
            default:
                maxCount = spaceCapacityConfig.getMaxCount();
                maxSize = spaceCapacityConfig.getMaxSize();
                space.setMaxCount(maxCount);
                space.setMaxSize(maxSize);
                break;
        }
    }

    /**
     * 空间权限校验
     *
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可访问
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

}
