package com.adventure.picturebackend.common.resp;

import com.adventure.picturebackend.common.utils.ErrorCode;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
    public BaseResponse(T data) {
        this.code = 0;
        this.data = data;
        this.message = "ok";
    }
    public BaseResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.data = null;
        this.message = errorCode.getMessage();
    }
    public BaseResponse(ErrorCode errorCode,String message) {
        this.code = errorCode.getCode();
        this.data = null;
        this.message = message;
    }


}
