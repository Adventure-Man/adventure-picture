package com.adventure.picturebackend.controller;

import com.adventure.picturebackend.aop.annotation.AuthCheck;
import com.adventure.picturebackend.common.constant.UserConstant;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.utils.ResultUtils;
import com.adventure.picturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
* @author Adventure
* @date 2025/7/13
* @description TODO
*/
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    private final CosManager cosManager;
    public FileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }
    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", originalFilename);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        }
        catch (Exception e) {
            log.error("file upload error, filepath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SERVER_ERROR, "上传失败");
        }
        finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filePath);
                }
            }
        }
    }

}
