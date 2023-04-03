package com.ark.monitor.bug.repoter.job.realJob;

import com.ark.monitor.bug.repoter.error.service.IQrtzTriggersService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PersistJobDataAfterExecution//持久化
@DisallowConcurrentExecution//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
public class CheckSelfQuartzJobBean extends QuartzJobBean {

    @Autowired
    private IQrtzTriggersService qrtzTriggersService;


    /**
     * @describe
     * @param context 定时任务参数
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("....CheckSelfQuartzJobBean --开始....");
        try {
            qrtzTriggersService.sendJobErrorMsg();
        } catch (Exception e) {
            log.error("CheckSelfQuartzJobBean，", e);
        }
        log.info("....CheckSelfQuartzJobBean, trigger.key:{} -- 结束", context.getTrigger().getKey());
    }

}
