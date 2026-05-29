package com.adventure.picturebackend.common.utils;

import com.adventure.picturebackend.common.exception.ErrorCode;
import com.adventure.picturebackend.common.resp.BaseResponse;

/**
 * 统一返回结果工具类
 */
public class ResultUtils {

    private ResultUtils() {

    }

    public static <T> BaseResponse<T> success(int code, T data, String message) {
        return new BaseResponse<>(code, data, message);
    }

    ;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data);
    }

    ;

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, null, "ok");
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, T data) {
        return new BaseResponse<>(errorCode.getCode(), data, errorCode.getMessage());
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, T data, String message) {
        return new BaseResponse<>(errorCode.getCode(), data, message);
    }

}
