package com.junmoyu.infra.config.datasource;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis Plus 自动填充字段
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.debug("start insert fill ...");
        }
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.debug("start update fill ...");
        }
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }
}
