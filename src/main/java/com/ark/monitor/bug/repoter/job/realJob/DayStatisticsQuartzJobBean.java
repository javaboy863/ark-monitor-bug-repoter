package com.ark.monitor.bug.repoter.job.realJob;

import com.ark.monitor.bug.repoter.statistics.service.IStatisticsRealtimeService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PersistJobDataAfterExecution//持久化
@DisallowConcurrentExecution//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
public class DayStatisticsQuartzJobBean extends QuartzJobBean {
    @Autowired
    private IStatisticsRealtimeService iStatisticsRealtimeService;



    /**
     * 统计昨天错误信息
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("....RealtimeStatisticsQuartzJobBean --开始....");
        try {
            iStatisticsRealtimeService.sendYesterdayErrorStatistics();
        } catch (Exception e) {
            log.error("RealtimeStatisticsQuartzJobBean，", e);
        }
        log.info("....RealtimeStatisticsQuartzJobBean, trigger.key:{} -- 结束", context.getTrigger().getKey());
    }

}
