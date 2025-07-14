package com.adventure.picturebackend.common.exception;

import com.adventure.picturebackend.common.utils.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * @author adventure
 *
 * @description 自定义业务异常
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public BusinessException(ErrorCode code) {
        super(code.getMessage());
        this.code = code.value();
    }

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code.value();
    }
}