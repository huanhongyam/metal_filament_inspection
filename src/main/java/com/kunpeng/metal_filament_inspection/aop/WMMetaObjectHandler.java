package com.kunpeng.metal_filament_inspection.aop;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WMMetaObjectHandler implements MetaObjectHandler {
    @Autowired
    private IdWorker idWorker;

    @Override
    public void insertFill(MetaObject metaObject) {
        Object id = this.getFieldValByName("batchNumber", metaObject);
        if (id == null) {
            Long generatedId = idWorker.generateId(SystemConstants.WIRE_MATERIAL_PREFIX);
            log.debug("自动填充批次号: {}", generatedId);
            this.fillStrategy(metaObject, "batchNumber", generatedId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时不处理
    }
}