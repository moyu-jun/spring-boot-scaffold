package com.junmoyu.iam.model.converter;

import com.junmoyu.iam.model.entity.RoleEntity;
import com.junmoyu.iam.model.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色实体转换器
 */
@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    RoleResponse toResponse(RoleEntity entity);
}
