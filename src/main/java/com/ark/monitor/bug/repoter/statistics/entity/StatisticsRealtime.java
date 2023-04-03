package com.ark.monitor.bug.repoter.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_statistics_realtime")
public class StatisticsRealtime {

    @TableId(value = "id",
            type = IdType.AUTO)
    private Long id;

    /**
     * 状态 0:待认领 1:处理中 2:处理完成 3:忽略
     */
    private Integer status;

    private Long msgId;

    private Integer errorCount;

    private String projectName;

    private String handler;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
