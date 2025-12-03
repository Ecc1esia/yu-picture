package com.github.ecc1esia.picture.infrastructure.aop;


import com.github.ecc1esia.picture.application.service.UserApplicationService;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.domain.user.valueobject.UserRoleEnum;
import com.github.ecc1esia.picture.infrastructure.annotation.AuthCheck;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserApplicationService userApplicationService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前用户
        User loginUser = userApplicationService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 判断是否需要权限
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 判断权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 管理员权限限制
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return joinPoint.proceed();
    }
}
