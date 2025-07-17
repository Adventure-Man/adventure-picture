package com.adventure.picturebackend.manager;

import com.adventure.picturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class CosManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private COSClient cosClient;

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 将本地文件上传到 COS
     * @param key 图片的 key
     * @param file 上传的
     * @return PutObjectResult 上传后的响应
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    };

    /**
     * 下载文件，从云存储中下载到本地
     *
     * @param key 文件的 key
     * @return the file
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

//    public UploadPictureResult uploadPicture(String imageUrl, String uploadPathPrefix) throws IOException {
//        // 1.校验图片
////        FileManagerTemplate.getCheckImageInfo(multipartFile);
//        validPicture(imageUrl);
////        String fileName = multipartFile.getOriginalFilename();
//        String fileName = FileUtil.mainName(imageUrl);
//        String suffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : ".jpg";
//        // 2.图片上传地址
//        String randomString = IdUtil.fastSimpleUUID();
//        String filePath = String.format("%s/%s%s", DateUtil.formatDate(new Date()), randomString, suffix);
//        // 2.1创建图片上传路径
//        String uploadPath = String.format("/%s/%s", uploadPathPrefix, filePath);
//        File file = null;
//        try {
//            // 2.2创建临时文件
//            file = File.createTempFile(filePath, null);
////            multipartFile.transferTo(file);
//            // 2.3上传图片
//            PutObjectResult putObjectResult = putPictureObject(uploadPath, file);
//            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
//            // 3.封装图片返回信息
//            UploadPictureResult uploadPictureResult = new UploadPictureResult();
//            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
//            int height = imageInfo.getHeight();
//            int width = imageInfo.getWidth();
//            uploadPictureResult.setPicName(FileUtil.mainName(fileName));
//            uploadPictureResult.setPicSize(FileUtil.size(file));
//            uploadPictureResult.setPicWidth(width);
//            uploadPictureResult.setPicHeight(height);
//            uploadPictureResult.setPicFormat(imageInfo.getFormat());
//            // 宽高比
//            uploadPictureResult.setPicScale(Math.round(height * 1.0 / width * 100) / 100.0);
//            return uploadPictureResult;
//        } catch (Exception e) {
//            log.error("file upload error, filepath = " + filePath, e);
//            throw new BusinessException(ErrorCode.SERVER_RESPONSE_ERROR, "上传失败");
//        } finally {
//            removeTempFile(file, filePath);
//        }
//    }
//    private static void removeTempFile(File file, String filePath) {
//        if (file != null && file.exists()) {
//            // 删除临时文件
//            boolean delete = file.delete();
//            if (!delete) {
//                log.error("file delete error, filepath = " + filePath);
//            }
//        }
//    }
//    private void validPicture(String fileUrl) {
//        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
//
//        try {
//            // 1. 验证 URL 格式
//            new URL(fileUrl); // 验证是否是合法的 URL
//        } catch (MalformedURLException e) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
//        }
//
//        // 2. 校验 URL 协议
//        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
//                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
//
//        // 3. 发送 HEAD 请求以验证文件是否存在
//        HttpResponse response = null;
//        try {
//            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
//            // 未正常返回，无需执行其他判断
//            if (response.getStatus() != HttpStatus.HTTP_OK) {
//                return;
//            }
//            // 4. 校验文件类型
//            String contentType = response.header("Content-Type");
//            if (StrUtil.isNotBlank(contentType)) {
//                // 允许的图片类型
//                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
//                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
//                        ErrorCode.PARAMS_ERROR, "文件类型错误");
//            }
//            // 5. 校验文件大小
//            String contentLengthStr = response.header("Content-Length");
//            if (StrUtil.isNotBlank(contentLengthStr)) {
//                try {
//                    long contentLength = Long.parseLong(contentLengthStr);
//                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
//                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
//                } catch (NumberFormatException e) {
//                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
//                }
//            }
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//    }

}
