package com.adventure.picturebackend.common.exception;

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

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }

    }
    public static void throwIf(ErrorCode errorCode, String message) {
            throw new BusinessException(errorCode, message);

    }
}
