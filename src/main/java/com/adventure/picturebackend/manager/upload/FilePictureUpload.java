package com.adventure.picturebackend.manager.upload;

import cn.hutool.core.util.StrUtil;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Adventure
 * @date 2025/7/18
 * @description 本地图片上传
 */
@Service
public class FilePictureUpload extends FileUploadTemplate {

    @Override
    protected String checkPicture(Object inputSource) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件为空");
        // 1.1 校验文件大小
        long size = multipartFile.getSize();
        final long oneMB = 1024 * 1024L;
        ThrowUtils.throwIf(size > oneMB * 2, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2MB");
        // 1.2 校验文件后缀
        String fileName = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(StrUtil.isBlank(fileName), ErrorCode.PARAMS_ERROR, "上传文件格式错误");
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        final String[] SUPPORT_FILE_SUFFIX = new String[]{".png", ".jpg", ".jpeg", ".gif", ".bmp"};
        // 忽略大小写检查文件后缀
        boolean isSupported = Arrays.stream(SUPPORT_FILE_SUFFIX).anyMatch(s -> s.equalsIgnoreCase(suffix));
        ThrowUtils.throwIf(!isSupported, ErrorCode.PARAMS_ERROR, "上传文件格式不支持");
//        ThrowUtils.throwIf(!Arrays.asList(SUPPORT_FILE_SUFFIX).contains(suffix), ErrorCode.PARAMS_ERROR, "上传文件格式不支持");
        // 1.3 校验图片信息
        BufferedImage read = ImageIO.read(multipartFile.getInputStream());
        ThrowUtils.throwIf(read == null, ErrorCode.PARAMS_ERROR, "上传文件不是图片");
        return suffix.toLowerCase();
    }

    @Override
    public String getSourceFileName(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();

    }

    @Override
    public void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);

    }
}
