package com.liyao.auth.model.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author 华总
 */
@Data
public class UserUpdatePassword implements Serializable {

    /**
     *  用户id
     */
    private Long id;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String confirmPassword;

    private static final long serialVersionUID = -4666106790694534042L;
}
