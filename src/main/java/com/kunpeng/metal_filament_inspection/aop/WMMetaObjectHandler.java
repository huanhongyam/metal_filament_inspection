package com.kunpeng.metal_filament_inspection.aop;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class WMMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 如果主键字段为空，自动填充雪花ID
        Object id = this.getFieldValByName("batchNumber", metaObject);
        if (id == null) {
            this.strictInsertFill(metaObject, "batchNumber", Long.class, IdWorker.getId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时不处理
    }
}