package com.adventure.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.adventure.picturebackend.api.aliyun.AliYunAiApi;
import com.adventure.picturebackend.api.aliyun.CreateOutPaintingTaskRequest;
import com.adventure.picturebackend.api.aliyun.CreateOutPaintingTaskResponse;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.utils.ColorSimilarUtils;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.manager.CosManager;
import com.adventure.picturebackend.manager.FileManager;
import com.adventure.picturebackend.manager.upload.FilePictureUpload;
import com.adventure.picturebackend.manager.upload.FileUploadTemplate;
import com.adventure.picturebackend.manager.upload.UrlPictureUpload;
import com.adventure.picturebackend.model.dto.picture.*;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.Space;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.PictureReviewStatusEnum;
import com.adventure.picturebackend.model.enums.SpaceLevelEnum;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.adventure.picturebackend.model.vo.UserVO;
import com.adventure.picturebackend.service.SpaceService;
import com.adventure.picturebackend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.adventure.picturebackend.mapper.PictureMapper;
import com.adventure.picturebackend.service.PictureService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 图片 服务层实现。
 *
 * @author Administrator
 * @since 2025-07-14
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Autowired
    private FileManager fileManager;

    @Autowired
    private UserService userService;

    // 定义锁的key
    public static final String LOCKKEY = "lock:picture:review:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AliYunAiApi aliYunAiApi;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder().initialCapacity(1024).maximumSize(10000L)
            // 缓存 5 分钟移除
            .expireAfterWrite(5L, TimeUnit.MINUTES).build();


    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        ThrowUtils.throwIf(count <= 0 || count > 30, ErrorCode.PARAMS_ERROR, "数量参数错误");
        String format = String.format("https://cn.bing.com/images/search?q=%s&mmasync=1", searchText);
        Document document = null;
        try {
            document = Jsoup.connect(format).get();
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new RuntimeException(e);
        }
        Elements imgElementList = document.getElementsByClass("imgpt");
        log.info("图片搜索结果: {}", document.html());
        ThrowUtils.throwIf(imgElementList.size() <= 0, ErrorCode.SERVER_RESPONSE_ERROR, "图片搜索失败");
        int uploadCount = 0;
        for (Element element : imgElementList) {
            Element imgElement = element.selectFirst("img.mimg");
            String imageUrl = null;
            if (imgElement != null) {
                imageUrl = imgElement.attr("src");
                log.info("图片地址: {}", imageUrl);
            }
            if (StrUtil.isBlank(imageUrl)) {
                log.info("图片地址为空: {}", imageUrl);
                continue;
            }
            // 处理图片url的格式
            int i = imageUrl.indexOf("?");
            imageUrl = i > 0 ? imageUrl.substring(0, i) : imageUrl;
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (!StrUtil.isBlank(namePrefix)) {
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(imageUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功: {}", pictureVO);
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        ThrowUtils.throwIf(uploadCount <= 0, ErrorCode.SERVER_RESPONSE_ERROR, "图片上传失败");
        return uploadCount;
    }

    // 上传图片
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) throws IOException {
        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否存在 null: 默认上传到用户公共空间 ！null: 上传到用户/团队空间
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 必须空间创建人（管理员）才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 校验空间额度
            Long maxCount = space.getMaxCount();
            Long maxSize = space.getMaxSize();
            Long totalCount = space.getTotalCount();
            Long totalSize = space.getTotalSize();
            ThrowUtils.throwIf(maxCount == null || maxSize == null, ErrorCode.SERVER_RESPONSE_ERROR, "空间额度信息缺失");
            if (totalCount >= maxCount || totalSize >= maxSize) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "空间额度不足");
            }

        }

        // ...
        // 按照用户 id 划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        // 根据 inputSource 类型区分上传方式 默认是本地上传的方式
        FileUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        // 保存图片信息
        Picture picture = Picture.builder().url(uploadPictureResult.getUrl()).thumbnailUrl(uploadPictureResult.getThumbnailUrl()).picWidth(uploadPictureResult.getPicWidth()).picHeight(uploadPictureResult.getPicHeight()).picSize(uploadPictureResult.getPicSize()).picFormat(uploadPictureResult.getPicFormat()).picScale(uploadPictureResult.getPicScale()).userId(loginUser.getId()).spaceId(spaceId).picColor(uploadPictureResult.getPicColor()).build();
        // 设置图片名称
        String picName = uploadPictureResult.getPicName();
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);

        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        //  通过编式事务保存图片信息,更新图片额度
        Boolean execute = transactionTemplate.execute((status) -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.SERVER_RESPONSE_ERROR, "上传图片信息失败");
            // 更新图片额度
            boolean res = spaceService.updateSpaceQuota(spaceId, uploadPictureResult.getPicSize(), 1, false);
            ThrowUtils.throwIf(!res, ErrorCode.SERVER_RESPONSE_ERROR, "更新图片额度失败");
            return true;
        });
        ThrowUtils.throwIf(execute == null || !execute, ErrorCode.SERVER_RESPONSE_ERROR, "上传图片信息失败");
        PictureVO pictureVO = PictureVO.objToVo(picture);
        UserVO userVO = userService.getUserVO(loginUser);
        pictureVO.setUser(userVO);
        return pictureVO; // Return the correct type
    }

//    @Override
//    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
//        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_PERMISSION, "用户没有权限");
//        Long id = null;
//        if (pictureUploadRequest != null) {
//            id = pictureUploadRequest.getId();
//        }
//        // 修改图片信息, 仅本人和管理员可修改
//        if (id != null) {
//            Picture one = this.getOne(QueryWrapper.create().eq(Picture::getId, id));
//            ThrowUtils.throwIf(one == null, ErrorCode.SERVER_RESPONSE_ERROR, "图片不存在");
//            ThrowUtils.throwIf(!one.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_PERMISSION);
//        }
//        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
//        // 上传图片
//        UploadPictureResult uploadPictureResult = null;
//        // 根据 inputSource 类型区分上传方式
//        try {
//
//            uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
//        } catch (IOException e) {
//            log.error("上传图片失败", e);
//            throw new BusinessException(ErrorCode.SERVER_RESPONSE_ERROR, "上传失败");
//        }
//        // 保存图片信息
//        Picture picture = Picture.builder()
//                .url(uploadPictureResult.getUrl())
//                .name(uploadPictureResult.getPicName())
//                .picWidth(uploadPictureResult.getPicWidth())
//                .picHeight(uploadPictureResult.getPicHeight())
//                .picSize(uploadPictureResult.getPicSize())
//                .picFormat(uploadPictureResult.getPicFormat())
//                .picScale(uploadPictureResult.getPicScale())
//                .userId(loginUser.getId())
//                .thumbnailUrl(uploadPictureResult.getThumbnailUrl())
//                .build();
//        // 填充审核参数
//        this.fillReviewParams(picture, loginUser);
//        if (id != null) {
//            picture.setId(id);
//            // 更新指定编辑时间
//            picture.setEditTime(LocalDateTime.now());
//        }
//        boolean result = this.saveOrUpdate(picture);
//        ThrowUtils.throwIf(!result, ErrorCode.SERVER_RESPONSE_ERROR, "保存图片信息失败");
//        return PictureVO.objToVo(picture);
//    }

    @Override
    public QueryWrapper getQueryWrapper(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        LocalDateTime startEditTime = pictureQueryRequest.getStartEditTime();
        LocalDateTime endEditTime = pictureQueryRequest.getEndEditTime();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();

        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }


        queryWrapper.select("*");
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(Picture::getName).like(searchText).or(Picture::getIntroduction).like(searchText);
//            queryWrapper.and(qw -> qw.like("name", searchText)
//                    .or()
//                    .like("introduction", searchText)
        }
        queryWrapper.eq("id", id, ObjUtil.isNotEmpty(id));
        queryWrapper.eq("userId", userId, ObjUtil.isNotEmpty(userId));
        queryWrapper.like("name", name, ObjUtil.isNotEmpty(name));
        queryWrapper.like("introduction", introduction, ObjUtil.isNotEmpty(introduction));
        queryWrapper.like("picFormat", picFormat, ObjUtil.isNotEmpty(picFormat));
        queryWrapper.eq("category", category, ObjUtil.isNotEmpty(category));
        queryWrapper.eq("picWidth", picWidth, ObjUtil.isNotEmpty(picWidth));
        queryWrapper.eq("picHeight", picHeight, ObjUtil.isNotEmpty(picHeight));
        queryWrapper.eq("picSize", picSize, ObjUtil.isNotEmpty(picSize));
        queryWrapper.eq("picScale", picScale, ObjUtil.isNotEmpty(picScale));
        // 普通用户只能查询审核通过的图片
        queryWrapper.eq("reviewerId", reviewerId, ObjUtil.isNotEmpty(reviewerId));
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue(), ObjUtil.isNotEmpty(reviewStatus));
        queryWrapper.eq("reviewMessage", reviewMessage, ObjUtil.isNotEmpty(reviewMessage));
        // 是否查询空间图片 null:是公共空间
        queryWrapper.eq("spaceId", spaceId, ObjUtil.isNotEmpty(spaceId));
        queryWrapper.isNull("spaceId", nullSpaceId);
        // 时间范围查询
        queryWrapper.ge("editTime", startEditTime, ObjectUtil.isNotEmpty(startEditTime));
        queryWrapper.lt("editTime", endEditTime, ObjectUtil.isNotEmpty(endEditTime));
        // JSON 数组查询 如 ["tag1", "tag2"] 查询tag1 或 tag2
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(new QueryOrderBy(new QueryColumn(sortField), sortOrder));
        }


        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }


    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public Page<PictureVO> getPictureVoList(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> records = picturePage.getRecords();
        Page<PictureVO> picturePageVO = new Page<>(picturePage.getPageNumber(), picturePage.getPageSize(), picturePage.getTotalRow());
        if (records == null || records.isEmpty()) {
            return picturePageVO;
        }
        Set<Long> collect = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(collect);
        Map<Long, UserVO> userVOMap = users.stream().map(user -> userService.getUserVO(user)).collect(Collectors.toMap(UserVO::getId, userVO -> userVO));
        List<PictureVO> pictureVOList = records.stream().map(picture -> {
            PictureVO pictureVO = PictureVO.objToVo(picture);
            Long userId = picture.getUserId();
            UserVO userVO = userVOMap.get(userId);
            pictureVO.setUser(userVO);
            return pictureVO;
        }).toList();
        picturePageVO.setRecords(pictureVOList);
        return picturePageVO;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(id == null || reviewStatus == null || reviewStatus == PictureReviewStatusEnum.REVIEWING.getValue(), ErrorCode.PARAMS_ERROR);
        try {
            Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(LOCKKEY + id, "locked", 2, TimeUnit.SECONDS);
            ThrowUtils.throwIf(!Boolean.TRUE.equals(isLocked), ErrorCode.OPERATION_ERROR, "获取锁失败，操作失败");
            Picture picture = this.getById(id);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
            // 已经是审核状态
            if (Objects.equals(picture.getReviewStatus(), reviewStatus)) {
                ThrowUtils.throwIf(ErrorCode.OPERATION_ERROR, "图片已审核");
            }
            // 更新图片审核状态
            Picture updatePicture = new Picture();
            updatePicture.setId(id);
            updatePicture.setReviewStatus(reviewStatus);
            updatePicture.setReviewMessage(reviewMessage);
            updatePicture.setReviewerId(loginUser.getId());
            updatePicture.setReviewTime(LocalDateTime.now());
            boolean b = this.updateById(updatePicture);
            ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
            if (reviewStatus == PictureReviewStatusEnum.PASS.getValue()) {
                // 审核通过，更新用户积分+10
                Long userId = picture.getUserId();
                User user = userService.getById(userId);
                if (user != null) {
                    Integer score = user.getScore();
                    score += 10;
                    user.setScore(score);
                    userService.updateById(user);
                }
            }
            log.info("图片审核完成，图片id：{}，审核状态：{}，审核信息：{}", id, reviewStatus, reviewMessage);
        } finally {
            redisTemplate.delete(LOCKKEY + id);
        }
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(LocalDateTime.now());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Page<PictureVO> pageVOCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 构建缓存key, 然后从缓存查询
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        String key = String.format("picture:list:page:vo:%s", DigestUtils.md5Hex(jsonStr));
        String cacheValue;
        try {
            cacheValue = LOCAL_CACHE.getIfPresent(key);
            if (cacheValue != null) {
                Page<PictureVO> bean = JSONUtil.toBean(cacheValue, Page.class);
                return bean;
            }
            cacheValue = redisTemplate.opsForValue().get(key);
            if (cacheValue != null) {
                LOCAL_CACHE.put(key, cacheValue);
                Page<PictureVO> bean = JSONUtil.toBean(cacheValue, Page.class);
                return bean;
            }
        } catch (Exception e) {
            log.error("缓存读取异常:{},缓存key:{}", e, key);
        }
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(pictureQueryRequest, request));
        Page<PictureVO> pictureVOPage = this.getPictureVoList(picturePage, request);
        // 存入Redis缓存
        // 设置10——20分钟过期
        try {
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(pictureVOPage), new Random().nextInt(10) + 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis缓存设置异常:{},缓存key:{}", e, key);
        }
        return pictureVOPage;
    }

    /**
     * 异步清理图片文件
     *
     * @param oldPicture
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.getMapper().selectCountByQuery(new QueryWrapper().eq(Picture::getUrl, pictureUrl));
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        try {
            // 提取路径部分
            String picturePath = new URL(pictureUrl).getPath();
            cosManager.deleteObject(picturePath);

            // 清理缩略图
            String thumbnailUrl = oldPicture.getThumbnailUrl();
            if (StrUtil.isNotBlank(thumbnailUrl)) {
                String thumbnailPath = new URL(thumbnailUrl).getPath();
                cosManager.deleteObject(thumbnailPath);
            }
        } catch (MalformedURLException e) {
            log.error("处理图片删除时遇到格式错误的 URL。图片 URL: {}", pictureUrl, e);
            throw new BusinessException(ErrorCode.SERVER_ERROR, "格式错误的 URL");
        }
    }

    @Override
    public Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, User loginUser) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体类和dto转换, 将List<String> tags转为JSON字符串
        Picture picture = PictureUpdateRequest.dtoToObj(pictureUpdateRequest);
        Long spaceId = picture.getSpaceId();
        Long pictureId = picture.getId();

        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原有图片一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }

        // 校验图片信息(格式，大小)
        this.validPicture(picture);
        // 判断更新的图片是否存在
        Picture picture1 = this.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(picture1 == null, ErrorCode.NOT_FOUND_ERROR);
        // 填充审核信息
//        User loginUser = userService.getById(picture1.getUserId());
        this.fillReviewParams(picture1, loginUser);
        // 更新图片信息
        boolean b = this.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return null;
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public boolean removeByPictureId(Long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.LOGIN_AUTH_ERROR, "用户未登录");
        // 判断图片是否存在
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或者管理员可以删除
//        if (!userService.isAdmin(loginUser) && !picture.getUserId().equals(loginUser.getId())) {
//            throw new BusinessException(ErrorCode.NOT_PERMISSION);
        this.checkPictureAuth(loginUser, picture);
        Long spaceId = picture.getSpaceId();
        Long picSize = picture.getPicSize();
        Boolean execute = transactionTemplate.execute((status) -> {
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.SERVER_RESPONSE_ERROR, "上传图片信息失败");
            // 更新图片额度
            boolean res = spaceService.updateSpaceQuota(spaceId, picSize, 1, true);
            ThrowUtils.throwIf(!res, ErrorCode.SERVER_RESPONSE_ERROR, "更新图片额度失败");
            return true;
        });
        ThrowUtils.throwIf(execute == null || !execute, ErrorCode.OPERATION_ERROR);
        this.clearPictureFile(picture);
        return true;
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 实体类和dto转换, 将List<String> tags转为JSON字符串
        Picture picture = PictureEditRequest.dtoToObj(pictureEditRequest);
        // 设置编辑时间
        picture.setEditTime(LocalDateTime.now());
        // 校验图片信息(格式，大小)
        this.validPicture(picture);
        // 判断更新的图片是否存在
        Picture odlPicture = this.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(odlPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 检查空间是否一致
//        if (ObjectUtil.isNull(picture.getSpaceId())) {
//            if (odlPicture.getSpaceId() != null){
//                picture.setSpaceId(odlPicture.getSpaceId());
//            }
//        }
//        if (ObjectUtil.notEqual(picture.getSpaceId(), odlPicture.getSpaceId())) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间不一致");
//        }

        // 仅当前用户和管理员可以编辑
//        User loginUser = userService.getLoginUser(request);
//        if (!odlPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NOT_PERMISSION);
//        }
        this.checkPictureAuth(loginUser, odlPicture);
        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        // 更新图片信息
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 3. 查询该空间下所有图片（必须有主色调）
        List<Picture> pictureList = this.list(new QueryWrapper().eq(Picture::getSpaceId, spaceId).isNotNull(Picture::getPicColor));
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        // 将目标颜色转为 Color 对象
        Color targetColor = Color.decode(picColor);
        // 4. 计算相似度并排序
        List<Picture> sortedPictures = pictureList.stream().sorted(Comparator.comparingDouble(picture -> {
                    // 提取图片主色调
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 取前 12 个
                .limit(12).toList();

        // 转换为 PictureVO
        return sortedPictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();


        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }

        // 3. 查询指定图片，仅选择需要的字段
        List<Picture> pictureList = this.list(new QueryWrapper()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
        );

        if (pictureList.isEmpty()) {
            return;
        }
        // 4. 更新分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });

        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);
        // 5. 批量更新
        boolean result = this.updateBatch(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * nameRule 格式：图片{序号}
     *
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }


    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        // 权限校验
        checkPictureAuth(loginUser, picture);
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }


}
