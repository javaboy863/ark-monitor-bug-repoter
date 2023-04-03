package com.ark.monitor.bug.repoter.job.realJob;

import com.ark.monitor.bug.repoter.error.service.IErrorDesService;
import com.ark.monitor.bug.repoter.error.service.IErrorMsgService;
import com.ark.monitor.bug.repoter.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@PersistJobDataAfterExecution//持久化
@DisallowConcurrentExecution//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
public class DeleteQuartzJobBean extends QuartzJobBean {
    @Autowired
    private IErrorMsgService errorMsgService;
    @Autowired
    private IErrorDesService errorDesService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            //删除30天前的数据
            LocalDateTime day = DateTimeUtil.getLastDay(-30);
            log.info("....DeleteQuartzJobBean -- 删除 {} 之前的日志开始执行....", day);
            errorMsgService.delInfo(day);
            errorDesService.delInfo(day);
            log.info("....DeleteQuartzJobBean --  任务：{}，删除日志结束", context.getTrigger().getKey());
        }catch (Exception e){
            log.error("",e);
        }
    }
}
