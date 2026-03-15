package com.liyao.auth.model.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 重置密码
 *
 * @author 华总
 */
@Data
public class UserResetPassword implements Serializable {

    /**
     *  用户id
     */
    private List<Long> id;

    private static final long serialVersionUID = -4666106790694534042L;
}
