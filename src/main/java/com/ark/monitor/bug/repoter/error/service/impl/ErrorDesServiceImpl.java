package com.ark.monitor.bug.repoter.error.service.impl;

import com.ark.monitor.bug.repoter.error.entity.ErrorDes;
import com.ark.monitor.bug.repoter.error.mapper.ErrorDesMapper;
import com.ark.monitor.bug.repoter.error.service.IErrorDesService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Service
public class ErrorDesServiceImpl extends ServiceImpl<ErrorDesMapper, ErrorDes> implements IErrorDesService {
    public void delInfo(LocalDateTime time) {
        this.remove(Wrappers.lambdaQuery(ErrorDes.class)
                .le(ErrorDes::getCreatedAt, time));
    }
}
