package com.liyao.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liyao.auth.model.entity.User;
import com.liyao.auth.model.user.UserQueryRequest;
import com.liyao.auth.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户 Mapper 接口
 * 定义用户相关的数据库操作方法
 *
 * @author liyao
 * @since 2025-04-11 12:19
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取用户分页列表
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return 返回值类型为 Page(UserVO) 的描述
     */
    Page<UserVO> getUserPage(Page<UserVO> page, UserQueryRequest queryParams);

    /**
     * 批量更新用户状态
     *
     * @param value 要更新的值
     * @param ids 用户 ID 列表
     * @return 返回整数值
     */
    int updateBatchValueByIds(@Param("value") String value, @Param("ids") List<Long> ids);
}
