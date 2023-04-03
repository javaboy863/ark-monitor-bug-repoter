package com.ark.monitor.bug.repoter.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class BaseEntity {



    @TableId(value = "id",
            type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

//    /**
//     * 更新时间
//     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private LocalDateTime updatedAt;


}
