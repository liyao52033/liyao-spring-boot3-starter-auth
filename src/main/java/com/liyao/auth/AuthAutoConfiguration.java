package com.liyao.auth;

import com.liyao.auth.aop.LoginFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 认证自动配置类
 * 配置用户认证相关的 Bean 和组件扫描
 */
@Configuration
@EnableConfigurationProperties(LoginProperties.class)
@MapperScan("com.liyao.auth.mapper")
@ComponentScan({ "com.liyao.auth.service", "com.liyao.auth.aop", "com.liyao.auth.enums", "com.liyao.auth.model" })
public class AuthAutoConfiguration {

    /**
     * 创建登录过滤器 Bean
     * 该方法使用 Spring 的@Bean 注解将方法返回值注册为 Spring 容器中的 Bean
     * 使用@ConditionalOnMissingBean 注解确保 Spring 容器中不存在同类型的 Bean 时才创建
     * 使用@ConditionalOnProperty 注解确保只有在配置文件中启用了 liyao.auth.enabled 属性时才创建
     *
     * @param loginProperties 登录配置属性，用于初始化 LoginFilter 的允许访问路径
     * @return 返回值类型为 LoginFilter 的描述
     */
    @Bean
    @ConditionalOnProperty(prefix = "liyao.auth", value = "enabled")
    @ConditionalOnMissingBean
    public LoginFilter loginFilter(LoginProperties loginProperties) {
        return new LoginFilter(loginProperties);
    }
}
