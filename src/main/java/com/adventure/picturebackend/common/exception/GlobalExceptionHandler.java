package com.adventure.picturebackend.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.utils.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author adventure
 *
 * @description 全局处理异常
 **/
@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    // 拦截：未登录异常
    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<BaseResponse<?>> handlerException(SaTokenException e) {
        ErrorCode errorCode = null;
        String message = e.getMessage();
        if(e instanceof NotLoginException){
            log.info("NotLoginException:{} - msg:{}",e,message);
            errorCode = ErrorCode.LOGIN_AUTH_ERROR;
        }
        if(e instanceof NotRoleException || e instanceof NotPermissionException){
            log.info("NotPermission:{} - msg:{}",e,message);
            errorCode = ErrorCode.NOT_PERMISSION;
        }
        return ResponseEntity.ok(new BaseResponse<>(errorCode,"token权限异常:" +message));
    }

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException：{} - msg:{}", e, e.getMessage());
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SERVER_RESPONSE_ERROR, "业务异常:"+e.getMessage()));
    }

    // 全局异常拦截,  拦截：其它所有异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handlerException(Exception e) {
        log.info("Exception:{} - msg:{}", e, e.getMessage());
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SERVER_ERROR,"系统异常:" + e.getMessage()));
    }

    // 请求参数错误
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        log.error("ValidationException:{} - msg:{}", ex, errors);
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.PARAMS_ERROR,"参数异常："+ex.getMessage()));
    }

}

