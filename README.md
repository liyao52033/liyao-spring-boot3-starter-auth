# Spring Boot 3 认证模块

## 简介

`liyao-spring-boot3-starter-auth` 是一个基于 Spring Boot 3 的认证组件，提供了用户认证、权限校验等功能，可以快速集成到 Spring Boot 3 项目中，简化认证和授权的开发工作。

## 功能特性

- 用户登录认证
- 基于注解的权限校验
- 可配置的路径过滤
- 支持用户角色管理
- 与 Spring Boot 3 无缝集成

## 安装方法

在项目的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.liyao52033</groupId>
    <artifactId>liyao-spring-boot3-starter-auth</artifactId>
    <version>1.2.0</version>
</dependency>
```

## 配置说明

在 `application.yml` 或 `application.properties` 中添加以下配置：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo
    username: xxxx
    password: xxxxx

liyao:
  auth:
    # 是否启用认证模块，默认为 true
    enabled: true
    # 允许直接访问的路径列表，不需要登录验证
    allowed-paths:
      - /api/public/**
      - /api/health/**
```

## 使用示例

### 1. 用户登录认证

认证模块会自动拦截需要认证的请求，默认情况下，除了配置的 `allowed-paths` 和内置的一些路径（如 Swagger、登录注册接口等）外，其他接口都需要登录后才能访问。

### 2. 基于注解的权限校验

使用 `@AuthCheck` 注解可以对方法进行权限校验：

```java
import com.liyao.auth.annotation.AuthCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    // 需要管理员角色才能访问
    @AuthCheck(mustRole = "admin")
    @GetMapping("/api/admin/users")
    public Result listUsers() {
        // 业务逻辑
        return Result.success();
    }
    
    // 需要普通用户角色才能访问
    @AuthCheck(mustRole = "user")
    @GetMapping("/api/user/info")
    public Result getUserInfo() {
        // 业务逻辑
        return Result.success();
    }
}
```

### 3. 获取当前登录用户

在 Service 或 Controller 中注入 `SysUserService`，然后使用 `getLoginUser` 方法获取当前登录用户：

```java
import com.liyao.auth.model.entity.User;
import com.liyao.auth.service.SysUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Resource
    private SysUserService sysUserService;
    
    @GetMapping("/api/user/current")
    public Result getCurrentUser(HttpServletRequest request) {
        User loginUser = sysUserService.getLoginUser(request);
        // 业务逻辑
        return Result.success(loginUser);
    }
}
```

### 4. 继承实体类并添加新字段

可以通过继承实体类来扩展用户信息，以下示例展示了如何创建新的实体类并添加额外字段：

```java
import com.liyao.auth.model.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 继承User类并添加新字段
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedUser extends User {
    // 新增手机号字段
    private String phone;
    
    // 新增地址字段
    private String address;
}
```

在使用时，可以通过`SysUserService`的`getLoginUser`方法获取扩展后的用户信息：

```java
@RestController
public class UserController {

    @Resource
    private SysUserService sysUserService;
    
    @GetMapping("/extended")
    public BaseResponse<ExtendedUser> getExtendedUser(HttpServletRequest request) {
        ExtendedUser extendedUser = (ExtendedUser) sysUserService.getLoginUser(request);
        // 业务逻辑
        return ResultUtils.success(extendedUser);
    }
}
```

### 5. 继承SysUserService扩展功能

可以通过继承`SysUserService`类来扩展服务功能，以下示例展示了如何添加自定义方法：

```java
import com.liyao.auth.service.SysUserService;
import com.liyao.auth.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public class CustomUserService extends SysUserService {

    /**
     * 自定义方法：根据用户ID获取用户详细信息
     * @param userId 用户ID
     * @return 用户详细信息
     */
    public UserDetailVO getUserDetail(long userId) {
        // 自定义业务逻辑
        return new UserDetailVO();
    }

    /**
     * 重写获取登录用户方法
     * @param request HTTP请求
     * @return 登录用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 自定义获取登录用户逻辑
        return super.getLoginUser(request);
    }
}
```

### 6. 自定义认证逻辑

如果需要自定义认证逻辑，可以通过实现自己的 `LoginFilter` 并注册为 Bean 来覆盖默认实现：

```java
import com.liyao.auth.LoginProperties;
import com.liyao.auth.aop.LoginFilter;
import com.liyao.auth.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CustomAuthConfig {

    @Bean
    @Primary
    public LoginFilter customLoginFilter(LoginProperties loginProperties) {
        return new CustomLoginFilter(loginProperties);
    }
}

// 自定义登录过滤器实现
public class CustomLoginFilter implements LoginFilter {
    
    private final LoginProperties loginProperties;
    
    public CustomLoginFilter(LoginProperties loginProperties) {
        this.loginProperties = loginProperties;
    }
    
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 自定义获取登录用户的逻辑
        // 例如：从请求头中获取token，然后从缓存或数据库中获取用户信息
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // 这里实现自己的token验证和用户获取逻辑
        // ...
        
        return null; // 返回获取到的用户或null
    }
}
```



## 用户角色

认证模块内置了以下用户角色：

- `user`：普通用户
- `admin`：管理员
- `ban`：被封号用户

可以通过 `UserRoleEnum` 枚举类获取这些角色：

```java
import com.liyao.auth.enums.UserRoleEnum;

// 获取用户角色
String userRole = UserRoleEnum.USER.getValue(); // "user"
String adminRole = UserRoleEnum.ADMIN.getValue(); // "admin"
```

## SysUserService 服务方法

`SysUserService` 是认证模块的核心服务接口，提供了用户认证、权限管理等功能。以下是该接口提供的所有方法：

### 用户注册与登录

```java
// 用户注册
long userRegister(String userAccount, String userPassword, String checkPassword);

// 用户登录
LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

// 用户登出
boolean userLogout(HttpServletRequest request);
```

### 用户信息管理

```java
// 获取当前登录用户
User getLoginUser(HttpServletRequest request);

// 获取当前登录用户（允许未登录）
User getLoginUserPermitNull(HttpServletRequest request);

// 检查用户是否已登录
Boolean isLogin(HttpServletRequest request);

// 获取脱敏的登录用户信息
LoginUserVO getLoginUserVO(User user);

// 获取脱敏的用户信息
UserVO getUserVO(User user);

// 获取脱敏的用户信息列表
List<UserVO> getUserVO(List<User> userList);

// 获取分页的用户信息
Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request);
```

### 密码管理

```java
// 修改用户密码
long updateUserPassword(UserUpdatePassword userUpdatePassword);
```

### 权限管理

```java
// 检查当前用户是否为管理员
boolean isAdmin(HttpServletRequest request);

// 检查指定用户是否为管理员
boolean isAdmin(User user);
```

### 用户查询与批量操作

```java
// 获取用户查询条件包装器
QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

// 根据ID批量修改用户字段值
String updateBatchValue(List<Long> ids, String value);

// 批量删除用户
boolean deleteUsers(String ids);
```

### 使用示例

以下是在控制器中使用 `SysUserService` 各方法的示例：

```java
import com.liyao.auth.model.entity.User;
import com.liyao.auth.model.vo.LoginUserVO;
import com.liyao.auth.model.vo.UserVO;
import com.liyao.auth.service.SysUserService;
import com.liyao.auth.model.dto.UserUpdatePassword;
import com.liyao.auth.model.dto.UserQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private SysUserService sysUserService;
    
    // 用户注册
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        long userId = sysUserService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getCheckPassword());
        return ResultUtils.success(userId);
    }
    
    // 用户登录
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request, HttpServletRequest httpServletRequest) {
        LoginUserVO loginUserVO = sysUserService.userLogin(request.getUserAccount(), request.getUserPassword(), httpServletRequest);
        return ResultUtils.success(loginUserVO);
    }
    
    // 用户登出
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = sysUserService.userLogout(request);
        return ResultUtils.success(result);
    }
    
    // 获取当前登录用户
    @GetMapping("/current")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = sysUserService.getLoginUser(request);
        return ResultUtils.success(loginUser);
    }
    
    // 获取当前登录用户（允许未登录）
    @GetMapping("/current-permit-null")
    public BaseResponse<User> getLoginUserPermitNull(HttpServletRequest request) {
        User loginUser = sysUserService.getLoginUserPermitNull(request);
        return ResultUtils.success(loginUser);
    }
    
    // 检查用户是否已登录
    @GetMapping("/is-login")
    public BaseResponse<Boolean> isLogin(HttpServletRequest request) {
        Boolean isLogin = sysUserService.isLogin(request);
        return ResultUtils.success(isLogin);
    }
    
    // 获取脱敏的登录用户信息
    @GetMapping("/login-vo")
    public BaseResponse<LoginUserVO> getLoginUserVO(@RequestParam("userId") Long userId) {
        User user = sysUserService.getById(userId);
        LoginUserVO loginUserVO = sysUserService.getLoginUserVO(user);
        return ResultUtils.success(loginUserVO);
    }
    
    // 获取脱敏的用户信息
    @GetMapping("/vo")
    public BaseResponse<UserVO> getUserVO(@RequestParam("userId") Long userId) {
        User user = sysUserService.getById(userId);
        UserVO userVO = sysUserService.getUserVO(user);
        return ResultUtils.success(userVO);
    }
    
    // 获取脱敏的用户信息列表
    @GetMapping("/vo/list")
    public BaseResponse<List<UserVO>> getUserVOList(@RequestBody List<Long> userIds) {
        List<User> userList = sysUserService.listByIds(userIds);
        List<UserVO> userVOList = sysUserService.getUserVO(userList);
        return ResultUtils.success(userVOList);
    }
    
    // 获取分页的用户信息
    @GetMapping("/vo/page")
    public BaseResponse<Page<UserVO>> getUserVOPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = sysUserService.page(new Page<>(current, size), 
                sysUserService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = sysUserService.getUserVOPage(userPage, request);
        return ResultUtils.success(userVOPage);
    }
    
    // 修改用户密码
    @PostMapping("/update-password")
    public BaseResponse<Long> updateUserPassword(@RequestBody UserUpdatePassword userUpdatePassword) {
        long result = sysUserService.updateUserPassword(userUpdatePassword);
        return ResultUtils.success(result);
    }
    
    // 检查当前用户是否为管理员
    @GetMapping("/is-admin")
    public BaseResponse<Boolean> isAdmin(HttpServletRequest request) {
        boolean isAdmin = sysUserService.isAdmin(request);
        return ResultUtils.success(isAdmin);
    }
    
    // 检查指定用户是否为管理员
    @GetMapping("/is-admin-by-user")
    public BaseResponse<Boolean> isAdminByUser(@RequestParam("userId") Long userId) {
        User user = sysUserService.getById(userId);
        boolean isAdmin = sysUserService.isAdmin(user);
        return ResultUtils.success(isAdmin);
    }
    
    // 根据ID批量修改用户字段值
    @PostMapping("/update-batch")
    public BaseResponse<String> updateBatchValue(@RequestBody BatchUpdateRequest request) {
        String result = sysUserService.updateBatchValue(request.getIds(), request.getValue());
        return ResultUtils.success(result);
    }
    
    // 批量删除用户
    @DeleteMapping("/delete-batch")
    public BaseResponse<Boolean> deleteUsers(@RequestParam("ids") String ids) {
        boolean result = sysUserService.deleteUsers(ids);
        return ResultUtils.success(result);
    }
}
```

## 注意事项

1. 该认证模块依赖于 Spring Boot 3，不兼容 Spring Boot 2.x 版本。
2. 默认情况下，认证模块会自动扫描 `com.liyao.auth.mapper`、`com.liyao.auth.service`、`com.liyao.auth.aop` 和 `com.liyao.auth.model` 包下的组件。
3. 认证模块使用了 MyBatis-Plus，需要确保项目中已经配置了数据源。

## 许可证

[MIT License](https://opensource.org/licenses/MIT)