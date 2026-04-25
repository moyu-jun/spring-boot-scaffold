package com.junmoyu.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.response.UserDetailResponse;
import com.junmoyu.iam.model.response.UserResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户ID获取用户全部角色标识
     *
     * @param userId 用户ID
     * @return 全部角色标识
     */
    List<String> getAllRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取用户全部权限标识
     *
     * @param userId 用户ID
     * @return 全部权限标识
     */
    List<String> getAllPermissionByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户列表（关联组织表）
     *
     * @param page     分页参数
     * @param keywords 搜索关键词
     * @return 用户分页数据
     */
    IPage<UserResponse> selectUserPage(Page<UserResponse> page, @Param("keywords") String keywords);

    /**
     * 查询用户详情（关联组织表）
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserDetailResponse selectUserDetail(@Param("id") Long id);
}