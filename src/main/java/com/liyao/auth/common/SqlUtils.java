package com.liyao.auth.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * SQL 工具类
 * 提供 SQL 相关的安全校验方法
 */
@Slf4j
@Component
public class SqlUtils {

    /**
     * 校验排序字段是否合法，防止 SQL 注入
     * 合法的排序字段不能包含特殊字符如 =、(、)、空格等
     *
     * @param sortField 排序字段
     * @return 返回布尔值，true 或 false
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
