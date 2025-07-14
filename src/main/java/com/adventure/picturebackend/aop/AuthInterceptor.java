package com.adventure.picturebackend.aop;

import com.adventure.picturebackend.aop.annotation.AuthCheck;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.UserRoleEnum;
import com.adventure.picturebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Adventurep
 * @date 2025/7/6
 * @description 权限认证aop
 */
@Aspect
@Component
public class AuthInterceptor {
    @Autowired
    private UserService userService;

    // 定义环绕通知
    @Around("@annotation(com.adventure.picturebackend.aop.annotation.AuthCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        AuthCheck authCheck = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(AuthCheck.class);
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes servletRequestAttributes =(ServletRequestAttributes)requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        this.check(authCheck, request);
        return joinPoint.proceed();
    }
    private void check(AuthCheck authCheck, HttpServletRequest request) {
        if (authCheck == null) {
            return;
        }
        String mustRole = authCheck.mustRole();
        if (StringUtils.isBlank(mustRole)){
            return;
        }
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            User loginUser = userService.getLoginUser(request);
            // 如果被封号，直接拒绝
            UserRoleEnum userRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
            if (userRole == null || UserRoleEnum.BAN.equals(userRole)) {
                throw new BusinessException(ErrorCode.NOT_PERMISSION);
            }
            // 如何接口要求admin,而用户不是admin，则拒绝
            if (UserRoleEnum.ADMIN.getValue().equals(mustRole) && !UserRoleEnum.ADMIN.equals(userRole)){
                throw new BusinessException(ErrorCode.NOT_PERMISSION);
            }
        }
    }

}
