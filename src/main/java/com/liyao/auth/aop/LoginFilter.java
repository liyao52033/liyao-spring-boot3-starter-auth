package com.liyao.auth.aop;

import com.liyao.auth.LoginProperties;
import com.liyao.auth.service.SysUserService;
import com.liyao.utility.exception.ErrorCode;
import com.liyao.utility.exception.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  登录过滤器
 */
@Slf4j
public class LoginFilter extends OncePerRequestFilter {

    @Resource
    private SysUserService sysUserService;

    // 指定允许通过的接口路径
    private static final List<String> DEFAULT_ALLOWED_PATHS = Arrays.asList(
    //swagger接口
    "/api/actuator/**",
    "/api/webjars/**",
    "/api/doc.html",
    "/api/favicon.ico",
    "/api/swagger-resources/**",
    "/api/v3/api-docs/**",
    "/api/swagger-ui/**",
    "/api/swagger-ui.html",
    "/api/ws/**",
    "/api/ws-app/**",
    "/api/v3/api-docs/swagger-config",
    "/api/v3/api-docs/default",

    // 用户接口
    "/api/user/login",
    "/api/user/register",
    "/api/user/refresh",
    "/api/user/getInfo",

    //验证码接口
    "/api/captcha/get",
    "/api/captcha/check",

    //授权文件接口
    "/api/license/generateLicense",
    "/api/license/uploadLicense",
    "/api/license/verifyLicense",
    "/api/license/getServerInfos"

    );

    private final Set<String> allowedPaths;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 构造函数，初始化允许通过的接口路径
     *
     * @param loginProperties 登录属性
     */
    public LoginFilter(LoginProperties loginProperties) {
        // 初始化时合并默认路径和配置文件中的路径
        Set<String> paths = new HashSet<>(DEFAULT_ALLOWED_PATHS);
        if (loginProperties.getAllowedPaths() != null) {
            paths.addAll(loginProperties.getAllowedPaths());
        }
        this.allowedPaths = paths;
    }

    /**
     * 根据条件判断返回不同结果
     *
     * @param request 参数 request 的描述
     * @param response 参数 response 的描述
     * @param filterChain 参数 filterChain 的描述
     * @throws ServletException 抛出 ServletException 异常的描述
     * @throws IOException 抛出 IOException 异常的描述
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equals("OPTIONS")) {
            // 如果是预检请求，直接放行
            filterChain.doFilter(request, response);
            return;
        }
        String requestURI = request.getRequestURI();
        // 检查请求的URL是否在允许列表中
        if (allowedPaths.stream().anyMatch(path -> antPathMatcher.match(path, requestURI))) {
            // 如果在允许列表中，则继续处理请求
            filterChain.doFilter(request, response);
        } else {
            try {
                sysUserService.getLoginUser(request);
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.error("接口 {} 触发了登录过滤器异常", requestURI, e);
                ResponseUtils.writeErrMsg(response, ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
            }
        }
    }
}
