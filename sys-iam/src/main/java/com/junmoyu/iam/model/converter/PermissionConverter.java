package com.junmoyu.iam.model.converter;

import com.junmoyu.iam.model.entity.PermissionEntity;
import com.junmoyu.iam.model.response.PermissionTreeNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 权限资源实体转换器
 */
@Mapper
public interface PermissionConverter {

    PermissionConverter INSTANCE = Mappers.getMapper(PermissionConverter.class);

    @Mapping(target = "children", ignore = true)
    PermissionTreeNode toTreeNode(PermissionEntity entity);
}
