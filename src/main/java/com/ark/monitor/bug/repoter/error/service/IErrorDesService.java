package com.ark.monitor.bug.repoter.error.service;

import com.ark.monitor.bug.repoter.error.entity.ErrorDes;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IErrorDesService extends IService<ErrorDes> {
    void delInfo(LocalDateTime time);
}
