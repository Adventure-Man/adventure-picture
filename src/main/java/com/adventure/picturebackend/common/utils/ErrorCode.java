package com.adventure.picturebackend.common.utils;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 *
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok",""),
    LOGIN_AUTH_ERROR(40001,"login_auth_error","token认证失败,请重新登录"),
    PARAMS_ERROR(40002,"request_param_error","请求参数错误"),
    NOT_PERMISSION(40003,"role_not_permission","无权限"),
    NETWORK_UNKNOW_ERROR(40004,"network_unknown_error","网络请求错误,请重试!"),
    SERVER_ERROR(50000,"server_error","服务器请求异常,请稍后再试!"),
    SERVER_RESPONSE_ERROR(50001,"server_response_error","业务异常!"),
    SERVER_NOT_FOUND(50002,"server_not_found","服务器未找到!"),
    NETWORK_AUTH_ERROR(50011,"Network Authentication Required","网络请求错误,请重试!"),
    ;

    @EnumValue
    private final int code;
    private final String message;
    private final  String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
    public int value() {
        return this.code;
    }
    public String message() {
        return this.message;
    }
}
