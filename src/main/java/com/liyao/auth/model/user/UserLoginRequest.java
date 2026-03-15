package com.liyao.auth.model.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 *
 *
 */
@Data
public class UserLoginRequest implements Serializable {

    /**用户名
     *
     */
    private String userAccount;

    /**密码
     *
     */
    private String userPassword;


    private static final long serialVersionUID = 3191241716373120793L;
}
