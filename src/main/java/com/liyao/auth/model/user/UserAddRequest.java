package com.liyao.auth.model.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 *
 *
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     */
    private String userRole;


    /**
     * 用户密码
     */
    private String userPassword = "1442de8d3dfff7f35f2a5f7c108b02b6";

    private static final long serialVersionUID = 1L;
}
