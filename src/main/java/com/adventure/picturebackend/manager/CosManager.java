package com.adventure.picturebackend.manager;

import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
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
     * 将本地文件上传到 COS
     * @param key the key
     * @param file the file
     * @return PutObjectResult
     * @throws CosClientException cosClientException
     * @throws CosServiceException cosServiceException
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PutObjectResult putObjectResult = null;
        try {
            putObjectResult = cosClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("Failed to upload file to COS", e);
            throw new BusinessException("Failed to upload file to COS");
        } finally {
            // 确认本进程不再使用 cosClient 实例之后，关闭即可
            cosClient.shutdown();
        }
        return putObjectResult;
    };
}
