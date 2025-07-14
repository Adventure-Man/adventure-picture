package com.adventure.picturebackend.common.utils;

import com.adventure.picturebackend.common.exception.BusinessException;

/**
 * @Author Adventure
 * @Date 2025/7/6
 * @Description 校验工具类
 */
public class ThrowUtils {
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }
}
