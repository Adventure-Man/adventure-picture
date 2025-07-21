package com.adventure.picturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import com.adventure.picturebackend.config.CosClientConfig;
import com.adventure.picturebackend.manager.upload.FilePictureUpload;
import com.adventure.picturebackend.manager.upload.UrlPictureUpload;
import com.adventure.picturebackend.model.dto.picture.PictureUploadRequest;
import com.adventure.picturebackend.model.dto.picture.UploadPictureResult;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.vo.PictureVO;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * 上传图片并获取图片信息
 */
@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    @Resource
    private COSClient cosClient;
    /**
     * 上传图片
     *
     * @param multipartFile    multipartFile
     * @param uploadPathPrefix uploadPathPrefix
     * @return COSObject
     * @throws IOException
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) throws IOException {
        // 1.校验图片
        FileManager.getCheckImageInfo(multipartFile);
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : ".jpg";
        // 2.图片上传地址
        String randomString = IdUtil.fastSimpleUUID();
        String filePath = String.format("%s/%s%s", DateUtil.formatDate(new Date()), randomString, suffix);
        // 2.1创建图片上传路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, filePath);
        File file = null;
        try {
            // 2.2创建临时文件
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            // 2.3上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 3.封装图片返回信息
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
            int height = imageInfo.getHeight();
            int width = imageInfo.getWidth();
            uploadPictureResult.setPicName(FileUtil.mainName(fileName));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            // 宽高比
            uploadPictureResult.setPicScale(Math.round(height * 1.0 / width * 100) / 100.0);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filePath, e);
            throw new BusinessException(ErrorCode.SERVER_RESPONSE_ERROR, "上传失败");
        } finally {
            removeTempFile(file, filePath);
        }


    }

    private static void removeTempFile(File file, String filePath) {
        if (file != null && file.exists()) {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error, filepath = " + filePath);
            }
        }
    }

    private static void getCheckImageInfo(MultipartFile multipartFile) throws IOException {
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件为空");
        // 1.1 校验文件大小
        long size = multipartFile.getSize();
        final long oneMB = 1024 * 1024L;
        ThrowUtils.throwIf(size > oneMB * 2, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2MB");
        // 1.2 校验文件后缀
        String fileName = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(StrUtil.isBlank(fileName), ErrorCode.PARAMS_ERROR, "上传文件格式错误");
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        final String[] SUPPORT_FILE_SUFFIX = new String[]{".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"};
        ThrowUtils.throwIf(!Arrays.asList(SUPPORT_FILE_SUFFIX).contains(suffix), ErrorCode.PARAMS_ERROR, "上传文件格式不支持");
        // 1.3 校验图片信息
        BufferedImage read = ImageIO.read(multipartFile.getInputStream());
        ThrowUtils.throwIf(read == null, ErrorCode.PARAMS_ERROR, "上传文件不是图片");
    }

}
