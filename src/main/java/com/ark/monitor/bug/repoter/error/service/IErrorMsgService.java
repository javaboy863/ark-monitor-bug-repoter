package com.ark.monitor.bug.repoter.error.service;

import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 */
public interface IErrorMsgService extends IService<ErrorMsg> {



    List getErrorCnt();

    List getErrorGroupCnt();

    void delInfo(LocalDateTime time);

    void statisticsErrorMsg();
}
