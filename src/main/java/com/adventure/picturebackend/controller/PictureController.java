package com.adventure.picturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.adventure.picturebackend.aop.annotation.AuthCheck;
import com.adventure.picturebackend.api.aliyun.AliYunAiApi;
import com.adventure.picturebackend.api.aliyun.CreateOutPaintingTaskResponse;
import com.adventure.picturebackend.api.aliyun.GetOutPaintingTaskResponse;
import com.adventure.picturebackend.api.imagesearch.ImageSearchResult;
import com.adventure.picturebackend.api.sub.ImageSearchApiFacade;
import com.adventure.picturebackend.common.constant.UserConstant;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.req.DeleteRequest;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ResultUtils;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.manager.upload.UrlPictureUpload;
import com.adventure.picturebackend.model.dto.picture.*;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.PictureTagCategory;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.PictureReviewStatusEnum;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.adventure.picturebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.adventure.picturebackend.service.PictureService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 图片 控制层。
 *
 * @author Administrator
 * @since 2025-07-14
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private UserService userService;

    @Autowired
    private UrlPictureUpload uploadPicture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AliYunAiApi aliYunAiApi;

    /**
     * 批量抓取图片
     *
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest request) throws IOException {
        User loginUser = userService.getLoginUser(request);
        String imageUrl = pictureUploadRequest.getImageUrl();
        PictureVO pictureVO = pictureService.uploadPicture(imageUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest, HttpServletRequest request) throws IOException {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 根据主键删除图片。
     *
     * @param deleteRequest 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> remove(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long pictureId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean res = pictureService.removeByPictureId(pictureId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 根据主键更新图片。(紧管理员)
     *
     * @param pictureUpdateRequest 图片dto
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 参数校验
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean flag = pictureService.updatePicture(pictureUpdateRequest, loginUser);
        return ResultUtils.success(flag);
    }

    /**
     * 根据图片主键获取详细信息。(不脱敏)
     *
     * @param id 图片主键
     * @return 图片详情
     */
    @GetMapping("getInfo/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getInfo(@PathVariable Long id, HttpServletRequest request) {
        // 参数校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询数据库
        Picture picture = pictureService.getById(id);
        if (picture == null) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        // 空间权限校验 只空间管理员查看
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        return ResultUtils.success(picture);
    }

    /**
     * 根据图片主键获取详细信息（脱敏）。
     *
     * @param id 图片主键
     * @return 图片详情
     */
    @GetMapping("/{id}")
    public BaseResponse<PictureVO> getInfoV2(@PathVariable Long id, HttpServletRequest request) {
        // 参数校验
        if (id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验 只空间管理员查看
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 用户编辑图片。
     *
     * @param pictureEditRequest 图片
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> edit(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 参数校验
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = pictureService.editPicture(pictureEditRequest, loginUser);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 分页查询图片信息。
     *
     * @param pictureQueryRequest 分页对象
     * @return 分页对象
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturePage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> page = pictureService.page(Page.of(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest, request));
        return ResultUtils.success(page);
    }


    /**
     * 分页获取图片列表。vo ,
     * 1.脱敏
     * 2.限制图片条数
     * 3.不用登录
     * @return 所有数据
     */
    @PostMapping("list/page/vo")
    public BaseResponse<Page<PictureVO>> listPicturePageVO(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest,request));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVoList(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取图片列表vo
     * 1.脱敏
     * 2.限制图片条数
     *
     * @return 所有数据
     */
    @PostMapping("list/page/vo/cache")
    @Deprecated
    public BaseResponse<Page<PictureVO>> listPicturePageVOCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<PictureVO> pictureVOPage = pictureService.pageVOCache(pictureQueryRequest, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 获取图片标签和分类。
     *
     * @return 图片标签和分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        // 使用nacos 配置中心配置热更新
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核。
     *
     * @param pictureReviewRequest 图片审核请求
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        return ResultUtils.success(resultList);
    }

    /**
     * 以颜色搜图
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 批量修改图片信息
     */
    @PostMapping("/edit/batch")
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }


}
