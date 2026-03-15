package com.liyao.auth.annotation;

import com.liyao.auth.enums.UserRoleEnum;
import com.liyao.auth.model.entity.User;
import com.liyao.auth.service.SysUserService;
import com.liyao.utility.exception.BusinessException;
import com.liyao.utility.exception.ErrorCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP 拦截器
 * 用于拦截带有@AuthCheck 注解的方法，进行权限校验
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private SysUserService sysUserService;

    /**
     * 环绕通知，拦截带有@AuthCheck 注解的方法
     *
     * @param joinPoint 连接点对象
     * @param authCheck 权限校验注解
     * @return 返回方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取 AuthCheck 注解中的必须角色
        String mustRole = authCheck.mustRole();
        // 获取当前请求对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = sysUserService.getLoginUser(request);
        // 校验是否有必须的权限
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            // 如果角色枚举不存在，则抛出异常
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果要封禁用户，则直接抛出异常
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.BAN_ERROR);
            }
            // 如果要管理员权限，则校验当前用户是否为管理员
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                // 如果当前用户不是管理员，则抛出异常
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 执行原方法
        return joinPoint.proceed();
    }
}