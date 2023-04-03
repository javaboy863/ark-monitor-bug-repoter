package com.ark.monitor.bug.repoter.error.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ark.monitor.bug.repoter.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_error_des")
public class ErrorDes extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * t_error的id
     */
    private Long errorId;

    /**
     * error类型id
     */
    private Integer errorType;

    /**
     * error类型说明
     */
    private String errorDes;
    /**
     * 解析处理结果  0未处理，1处理完成 -1无法解析
     */
    private Integer status;

}
