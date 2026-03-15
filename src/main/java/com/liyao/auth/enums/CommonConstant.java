package com.liyao.auth.enums;

import org.springframework.stereotype.Component;

/**
 * 通用常量
 *
 *
 *
 */
@Component
public interface CommonConstant {

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";

    /**
     * 系统配置 IP的QPS限流的KEY
     */
    String SYSTEM_CONFIG_IP_QPS_LIMIT_KEY = "IP_QPS_THRESHOLD_LIMIT";

}
