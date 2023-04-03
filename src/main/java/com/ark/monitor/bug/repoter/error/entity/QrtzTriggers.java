package com.ark.monitor.bug.repoter.error.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName IQrtzTriggersMapper
 * @Description
 * @Version 1.0
 */

@Data

@Accessors(chain = true)
@TableName("QRTZ_TRIGGERS")
public class QrtzTriggers  {

    @TableField("JOB_NAME")
    private String jobName;
    @TableField("TRIGGER_STATE")
    private String triggerState;


}
