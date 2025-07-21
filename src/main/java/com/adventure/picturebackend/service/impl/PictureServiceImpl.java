package com.adventure.picturebackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.manager.FileManager;
import com.adventure.picturebackend.manager.upload.FilePictureUpload;
import com.adventure.picturebackend.manager.upload.FileUploadTemplate;
import com.adventure.picturebackend.manager.upload.UrlPictureUpload;
import com.adventure.picturebackend.model.dto.picture.*;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.PictureReviewStatusEnum;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.adventure.picturebackend.model.vo.UserVO;
import com.adventure.picturebackend.service.UserService;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)){
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
            if (imgElement != null){
                imageUrl = imgElement.attr("src");
                log.info("图片地址: {}", imageUrl);
            }
            if(StrUtil.isBlank(imageUrl)){
                log.info("图片地址为空: {}", imageUrl);
                continue;
            }
            // 处理图片url的格式
            int i = imageUrl.indexOf("?");
            imageUrl = i > 0 ? imageUrl.substring(0, i) : imageUrl;
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if(!StrUtil.isBlank(namePrefix)){
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
            if(uploadCount >= count){
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
        // ...
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 根据 inputSource 类型区分上传方式 默认是本地上传的方式
        FileUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        // 保存图片信息
        Picture picture = Picture.builder()
                .url(uploadPictureResult.getUrl())
                .picWidth(uploadPictureResult.getPicWidth())
                .picHeight(uploadPictureResult.getPicHeight())
                .picSize(uploadPictureResult.getPicSize())
                .picFormat(uploadPictureResult.getPicFormat())
                .picScale(uploadPictureResult.getPicScale())
                .userId(loginUser.getId())
                .build();
        // 设置图片名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);

        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        boolean result = this.save(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SERVER_RESPONSE_ERROR, "上传图片信息失败");
        return PictureVO.objToVo(picture);
    }

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_PERMISSION, "用户没有权限");
        Long id = null;
        if (pictureUploadRequest != null) {
            id = pictureUploadRequest.getId();
        }
        // 修改图片信息, 仅本人和管理员可修改
        if (id != null) {
            Picture one = this.getOne(QueryWrapper.create().eq(Picture::getId, id));
            ThrowUtils.throwIf(one == null, ErrorCode.SERVER_RESPONSE_ERROR, "图片不存在");
            ThrowUtils.throwIf(!one.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_PERMISSION);
        }
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片
        UploadPictureResult uploadPictureResult = null;
        // 根据 inputSource 类型区分上传方式
        try {

            uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ErrorCode.SERVER_RESPONSE_ERROR, "上传失败");
        }
        // 保存图片信息
        Picture picture = Picture.builder()
                .url(uploadPictureResult.getUrl())
                .name(uploadPictureResult.getPicName())
                .picWidth(uploadPictureResult.getPicWidth())
                .picHeight(uploadPictureResult.getPicHeight())
                .picSize(uploadPictureResult.getPicSize())
                .picFormat(uploadPictureResult.getPicFormat())
                .picScale(uploadPictureResult.getPicScale())
                .userId(loginUser.getId())
                .build();
        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        if (id != null) {
            picture.setId(id);
            // 更新指定编辑时间
            picture.setEditTime(LocalDateTime.now());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SERVER_RESPONSE_ERROR, "保存图片信息失败");
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
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


        queryWrapper.select("*");
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(Picture::getName).like(searchText)
                    .or(Picture::getIntroduction).like(searchText);
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
        // 用户只能查询审核通过的图片
        queryWrapper.eq("reviewerId", reviewerId, ObjUtil.isNotEmpty(reviewerId));
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue(), ObjUtil.isNotEmpty(reviewStatus));
        queryWrapper.eq("reviewMessage", reviewMessage, ObjUtil.isNotEmpty(reviewMessage));
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


}
