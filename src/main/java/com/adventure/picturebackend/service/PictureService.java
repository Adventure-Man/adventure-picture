package com.adventure.picturebackend.service;

import com.adventure.picturebackend.model.dto.picture.PictureQueryRequest;
import com.adventure.picturebackend.model.dto.picture.PictureReviewRequest;
import com.adventure.picturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.adventure.picturebackend.model.dto.picture.PictureUploadRequest;
import com.adventure.picturebackend.model.entity.Picture;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 图片 服务层。
 *
 * @author Administrator
 * @since 2025-07-14
 */
public interface PictureService extends IService<Picture> {
    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


    /**
     * URL上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) throws IOException;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取单条数据转vo
     *
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 校验管理员修改的图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 获取图片列表
     *
     * @param picturePage
     * @return
     */
    Page<PictureVO> getPictureVoList(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 图片列表缓存
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    Page<PictureVO> pageVOCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);
}
