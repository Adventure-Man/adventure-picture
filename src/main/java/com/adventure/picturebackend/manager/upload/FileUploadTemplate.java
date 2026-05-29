package com.adventure.picturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.exception.ErrorCode;
import com.adventure.picturebackend.config.CosClientConfig;
import com.adventure.picturebackend.manager.CosManager;
import com.adventure.picturebackend.model.dto.picture.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 上传图片并获取图片信息
 *  模板方法设计模式:
 *  抽象类定义抽象方法，抽象方法由子类实现
 *  两种文件上传的流程相似, 所以抽象类定义抽象方法,将不同的实现逻辑交给子类实现
 */
@Slf4j
public abstract class FileUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    @Resource
    private COSClient cosClient;


    /**
     * 上传图片
     *
     * @param inputSource    inputSource
     * @param uploadPathPrefix uploadPathPrefix
     * @return COSObject
     * @throws IOException
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) throws IOException {
        // 1.校验图片
        String suffix =  checkPicture(inputSource);
        // 2.获取文件名
        String fileName = getSourceFileName(inputSource);
        // 2.0 图片上传地址
        String randomString = IdUtil.fastSimpleUUID();
        String filePath = String.format("%s/%s%s", DateUtil.formatDate(new Date()), randomString, suffix);
        // 2.1创建图片上传路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, filePath);
        File file = null;
        try {
            // 2.2创建临时文件
            file = File.createTempFile(filePath, null);
//            multipartFile.transferTo(file);
            processFile(inputSource, file);
            // 2.3上传图片到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (objectList != null && !objectList.isEmpty()){
                // 获取原图 压缩图
                CIObject compressedCiobject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thumbnailCiobject = compressedCiobject;
                // 如果存在缩略图
                if (objectList.size() > 1){
                    thumbnailCiobject = objectList.get(1);
                }
                // 获取缩略图
//                CIObject thumbnailCiobject = objectList.get(1);
                return getUploadPictureResult(uploadPath, compressedCiobject, fileName, thumbnailCiobject, imageInfo);
            }
            // 3.封装图片返回信息
            return getUploadPictureResult(uploadPath, imageInfo, fileName, file);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filePath, e);
            throw new BusinessException(ErrorCode.SERVER_RESPONSE_ERROR, "上传失败");
        } finally {
            removeTempFile(file, filePath);
        }


    }

    private UploadPictureResult getUploadPictureResult(String originFileName, CIObject compressedCiobject, String fileName, CIObject thumbnailCiobject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        Integer width = compressedCiobject.getWidth();
        Integer height = compressedCiobject.getHeight();
        long size = compressedCiobject.getSize().longValue();
        String format = compressedCiobject.getFormat();
        // 压缩图片地址 .webp
        uploadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ compressedCiobject.getKey());
        // 设置缩略图
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() +"/"+ thumbnailCiobject.getKey());
        // 图片名称
        uploadPictureResult.setPicName(FileUtil.mainName(fileName));
        uploadPictureResult.setPicSize(size);
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicFormat(format);
        // 宽高比
        uploadPictureResult.setPicScale(Math.round(height * 1.0 / width * 100) / 100.0);
        // 颜色
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }

    private UploadPictureResult getUploadPictureResult(String uploadPath, ImageInfo imageInfo, String fileName, File file) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
        int height = imageInfo.getHeight();
        int width = imageInfo.getWidth();
        // 图片名称
        uploadPictureResult.setPicName(FileUtil.mainName(fileName));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        // 宽高比
        uploadPictureResult.setPicScale(Math.round(height * 1.0 / width * 100) / 100.0);
        // 颜色
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }

    /**
     * 校验输入源
     * @param inputSource
     */
    protected abstract String checkPicture(Object inputSource) throws IOException;

    /**
     * 获取源文件名
     * @param inputSource
     */
    protected abstract String getSourceFileName(Object inputSource);

    /**
     * 处理输入源生成本地文件
     * @param file
     * @param inputSource
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    public static void removeTempFile(File file, String filePath) {
        if (file != null && file.exists()) {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error, filepath = " + filePath);
            }
        }
    }

}
