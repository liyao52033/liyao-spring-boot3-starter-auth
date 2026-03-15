package com.liyao.auth.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举类
 * 定义了系统中用户的三种角色：普通用户、管理员、被封禁用户
 */
@Getter
public enum UserRoleEnum {

    /**
     * USER - 普通用户角色
     */
    USER("普通用户", "user"),
    /**
     * ADMIN - 管理员角色
     */
    ADMIN("管理员", "admin"),
    /**
     * BAN - 被封禁用户角色
     */
    BAN("被封禁用户", "ban");

    /**
     * 角色文本描述
     */
    private final String text;

    /**
     * 角色值
     */
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取所有角色值的列表
     *
     * @return 返回字符串
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据角色值获取对应的枚举
     *
     * @param value 角色值
     * @return 返回值类型为 UserRoleEnum 的描述
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
