package com.adventure.picturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 空间容量配置:
 *              默认限制：最多100个文件，总大小不超过100MB
 *              普通用户(ordinary)：同默认限制
 *              专业用户(professional)：最多1000个文件，总大小不超过1GB
 *              旗舰用户(flagship)：最多10000个文件，总大小不超过10GB
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "space.capacity")
public class SpaceCapacityConfig {
    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;
    /**
     * 当前空间下的图片数量
     */
    private Long maxCount;

    // 添加不同套餐的配置
    private SpaceCapacity ordinary = new SpaceCapacity();
    private SpaceCapacity professional = new SpaceCapacity();
    private SpaceCapacity flagship = new SpaceCapacity();

    @Data
    public static class SpaceCapacity {
        /**
         * 空间图片的最大总大小
         */
        private Long maxSize;
        /**
         * 当前空间下的图片数量
         */
        private Long maxCount;

    }
}
