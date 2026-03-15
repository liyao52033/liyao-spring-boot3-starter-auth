package com.liyao.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "liyao.auth")
public class LoginProperties {

    /**
     * 是否开启
     */
    private Boolean enabled  = true;

    /**
     * 添加允许直接访问的路径，默认只添加了Knife4j相关路径
     */
    private List<String> allowedPaths;
}
