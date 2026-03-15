package com.liyao.auth.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liyao.auth.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象（脱敏）
 * 用于向前端返回用户信息，不包含敏感数据
 *
 * @author liyao
 * @since 2025-04-11 12:19
 */
@Data
@Schema(description = "用户视图对象")
public class UserVO implements Serializable {

    /**
     * 用户 id
     */
    @Schema(description = "用户 id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @Schema(description = "用户角色", example = "user")
    private String userRole;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Serial
    private static final long serialVersionUID = 7200363445787521896L;

    /**
     * 将 VO 对象转换为实体对象
     *
     * @param userVO 用户 VO 对象
     * @return 返回值类型为 User 的描述
     */
    public static User voToObj(UserVO userVO) {
        if (userVO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userVO, user);
        return user;
    }

    /**
     * 将实体对象转换为 VO 对象
     *
     * @param user 用户实体对象
     * @return 返回值类型为 UserVO 的描述
     */
    public static UserVO objToVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
