package com.junmoyu.iam.model.converter;

import com.junmoyu.iam.model.entity.OrgEntity;
import com.junmoyu.iam.model.response.OrgTreeNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 组织架构实体转换器
 */
@Mapper
public interface OrgConverter {

    OrgConverter INSTANCE = Mappers.getMapper(OrgConverter.class);

    @Mapping(target = "children", ignore = true)
    OrgTreeNode toTreeNode(OrgEntity entity);
}
