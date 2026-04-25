package com.junmoyu.iam.model.converter;

import com.junmoyu.iam.model.entity.UserGroupEntity;
import com.junmoyu.iam.model.response.UserGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户组实体转换器
 */
@Mapper
public interface UserGroupConverter {

    UserGroupConverter INSTANCE = Mappers.getMapper(UserGroupConverter.class);

    UserGroupResponse toResponse(UserGroupEntity entity);
}
