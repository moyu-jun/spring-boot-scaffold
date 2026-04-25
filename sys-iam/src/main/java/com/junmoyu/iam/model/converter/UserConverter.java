package com.junmoyu.iam.model.converter;

import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户实体转换器
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mapping(target = "orgName", ignore = true)
    UserResponse toResponse(UserEntity entity);
}
