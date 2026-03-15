package com.liyao.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyao.auth.common.SqlUtils;
import com.liyao.auth.enums.CommonConstant;
import com.liyao.auth.enums.UserRoleEnum;
import com.liyao.auth.mapper.UserMapper;
import com.liyao.auth.model.entity.User;
import com.liyao.auth.model.user.UserQueryRequest;
import com.liyao.auth.model.user.UserUpdatePassword;
import com.liyao.auth.model.vo.LoginUserVO;
import com.liyao.auth.model.vo.UserVO;
import com.liyao.auth.service.SysUserService;
import com.liyao.utility.exception.BusinessException;
import com.liyao.utility.exception.ErrorCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static com.liyao.auth.enums.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 * 提供用户注册、登录、密码修改、用户信息管理等核心功能
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<UserMapper, User> implements SysUserService {

    /**
     * 密码加盐值，用于增强密码安全性
     */
    private static final String SALT = "liyao";

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 返回整数值
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 参数校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和确认密码必须一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账号不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }
            // 2. 创建用户
            User user = new User();
            Long id = user.getId();
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            String accessKey = DigestUtils.md5DigestAsHex((SALT + id + userAccount).getBytes());
            String secretKey = DigestUtils.md5DigestAsHex((SALT + id + userAccount + userPassword).getBytes());
            // 3. 设置用户信息
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败，请重试");
            }
            return user.getId();
        }
    }

    /**
     * 更新用户密码
     *
     * @param userUpdatePassword 密码更新请求参数
     * @return 返回整数值
     */
    @Override
    public long updateUserPassword(UserUpdatePassword userUpdatePassword) {
        // 参数校验
        if (userUpdatePassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        Long id = userUpdatePassword.getId();
        String userPassword = userUpdatePassword.getUserPassword();
        String newPassword = userUpdatePassword.getNewPassword();
        String confirmPassword = userUpdatePassword.getConfirmPassword();
        if (StringUtils.isAnyBlank(userPassword, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        // 验证原密码是否正确
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("id", id);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
        }
        // 新密码长度校验
        if (newPassword.length() < 6 || confirmPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 新密码和确认密码必须一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 加密新密码
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 3. 更新密码
        User password = new User();
        password.setUserPassword(encryptNewPassword);
        password.setId(user.getId());
        boolean saveResult = this.updateById(password);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码更新失败，请重试");
        }
        return password.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP 请求对象
     * @return 返回值类型为 LoginUserVO 的描述
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求不能为空");
        }
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于 4 位");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于 6 位");
        }
        // 2. 验证密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 检查用户是否被封禁
        if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁，请联系管理员");
        }
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        // 3. 保存用户登录状态到 Session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP 请求对象
     * @return 返回值类型为 User 的描述
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求不能为空");
        }
        // 获取 Session 中的用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中重新查询用户信息，确保数据是最新的
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户信息（允许为 null）
     *
     * @param request HTTP 请求对象
     * @return 返回值类型为 User 的描述
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求不能为空");
        }
        // 获取 Session 中的用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库中重新查询用户信息，确保数据是最新的
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 判断用户是否已登录
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    @Override
    public Boolean isLogin(HttpServletRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求不能为空");
        }
        // 获取 Session 中的用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return false;
        }
        // 从数据库中重新查询用户信息，确保数据是最新的
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        return currentUser != null;
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求不能为空");
        }
        // 获取 Session 中的用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    /**
     * 判断指定用户是否为管理员
     *
     * @param user 用户实体
     * @return 返回布尔值，true 或 false
     */
    @Override
    public boolean isAdmin(User user) {
        // 判断用户角色是否为管理员
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户登出
     *
     * @param request HTTP 请求对象
     * @return 返回布尔值，true 或 false
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 移除 Session 中的用户登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 转换为登录用户 VO
     *
     * @param user 用户实体
     * @return 返回值类型为 LoginUserVO 的描述
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        // 参数校验
        if (user == null) {
            return null;
        }
        // 使用 JSON 序列化和反序列化的方式进行对象转换（脱敏处理）
        String jsonObject = JSON.toJSONString(user);
        LoginUserVO vo = JSON.parseObject(jsonObject, LoginUserVO.class);
        vo.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        return vo;
    }

    /**
     * 转换为用户 VO
     *
     * @param user 用户实体
     * @return 返回值类型为 UserVO 的描述
     */
    @Override
    public UserVO getUserVO(User user) {
        // 参数校验
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不能为空");
        }
        return UserVO.objToVo(user);
    }

    /**
     * 批量转换为用户 VO 列表
     *
     * @param userList 用户实体列表
     * @return 返回列表数据，类型为 List(UserVO)
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        // 参数校验
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取用户 VO 分页数据
     *
     * @param userPage 用户分页数据
     * @param request HTTP 请求对象
     * @return 返回值类型为 Page(UserVO) 的描述
     */
    @Override
    public Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request) {
        // 参数校验
        if (userPage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数不能为空");
        }
        List<User> userList = userPage.getRecords();
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        if (CollUtil.isEmpty(userList)) {
            return userVOPage;
        }
        // 对象转换 => 返回脱敏后的用户数据
        List<UserVO> userVOList = userList.stream().map(UserVO::objToVo).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 构建查询条件
     *
     * @param userQueryRequest 用户查询请求参数
     * @return 返回值类型为 QueryWrapper(User) 的描述
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 排序字段校验
        if (StringUtils.isBlank(sortOrder)) {
            sortOrder = CommonConstant.SORT_ORDER_ASC;
        }
        if (!CommonConstant.SORT_ORDER_ASC.equals(sortOrder) && !CommonConstant.SORT_ORDER_DESC.equals(sortOrder)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序类型错误");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户 ID 列表，多个 ID 用英文逗号分隔
     * @return 返回布尔值，true 或 false
     */
    @Override
    public boolean deleteUsers(String ids) {
        // 参数校验
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除用户 ID 列表不能为空");
        // 解析 ID 列表
        List<Long> idList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        return this.removeByIds(idList);
    }

    /**
     * 批量更新用户状态
     *
     * @param ids 用户 ID 列表
     * @param value 要更新的值
     * @return 返回字符串
     */
    @Override
    public String updateBatchValue(List<Long> ids, String value) {
        // 参数校验
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 ID 列表不能为空");
        }
        if (StringUtils.isBlank(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新值不能为空");
        }
        userMapper.updateBatchValueByIds(value, ids);
        return value;
    }
}
