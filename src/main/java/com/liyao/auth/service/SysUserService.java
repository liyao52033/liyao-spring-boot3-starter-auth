package com.liyao.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyao.auth.model.entity.User;
import com.liyao.auth.model.user.UserQueryRequest;
import com.liyao.auth.model.user.UserUpdatePassword;
import com.liyao.auth.model.vo.LoginUserVO;
import com.liyao.auth.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务接口
 * 定义用户相关的服务方法，包括注册、登录、权限校验等
 */
public interface SysUserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 返回整数值
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 更新用户密码
     *
     * @param userUpdatePassword 密码更新请求参数
     * @return 返回整数值
     */
    long updateUserPassword(UserUpdatePassword userUpdatePassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP 请求对象
     * @return 返回值类型为 LoginUserVO 的描述
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP 请求对象
     * @return 返回值类型为 User 的描述
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断用户是否已登录
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    Boolean isLogin(HttpServletRequest request);

    /**
     * 获取当前登录用户信息（允许为 null）
     *
     * @param request HTTP 请求对象
     * @return 返回值类型为 User 的描述
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 判断当前用户是否为管理员
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断指定用户是否为管理员
     *
     * @param user 用户实体
     * @return 返回布尔值，true 或 false
     */
    boolean isAdmin(User user);

    /**
     * 用户登出
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 转换为登录用户 VO
     *
     * @param user 用户实体
     * @return 返回值类型为 LoginUserVO 的描述
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 转换为用户 VO
     *
     * @param user 用户实体
     * @return 返回值类型为 UserVO 的描述
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户 VO 分页数据
     *
     * @param userPage 用户分页数据
     * @param request HTTP 请求对象
     * @return 返回值类型为 Page(UserVO) 的描述
     */
    Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request);

    /**
     * 批量转换为用户 VO 列表
     *
     * @param userList 用户实体列表
     * @return 返回列表数据，类型为 List(UserVO)
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 构建查询条件
     *
     * @param userQueryRequest 用户查询请求参数
     * @return 返回值类型为 QueryWrapper(User) 的描述
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 批量更新用户状态
     *
     * @param ids 用户 ID 列表
     * @param value 要更新的值
     * @return 返回字符串
     */
    String updateBatchValue(List<Long> ids, String value);

    /**
     * 批量删除用户
     *
     * @param ids 用户 ID 列表，多个 ID 用英文逗号分隔
     * @return 返回布尔值，true 或 false
     */
    boolean deleteUsers(String ids);
}
