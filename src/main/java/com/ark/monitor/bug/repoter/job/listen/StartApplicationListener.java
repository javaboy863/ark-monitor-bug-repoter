package com.ark.monitor.bug.repoter.job.listen;


import com.ark.monitor.bug.repoter.job.config.SchedulerConfig;
import com.ark.monitor.bug.repoter.job.realJob.CheckSelfQuartzJobBean;
import com.ark.monitor.bug.repoter.job.realJob.DayStatisticsQuartzJobBean;
import com.ark.monitor.bug.repoter.job.realJob.DeleteQuartzJobBean;
import com.ark.monitor.bug.repoter.job.realJob.RealtimeStatisticsQuartzJobBean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @Description:利用Quartz定时任务，可以在初始化上实现，
 */
@Slf4j
@Component
public class StartApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    SchedulerConfig schedulerConfig;
    public static AtomicInteger count = new AtomicInteger(0);
    private static String TRIGGER_GROUP_NAME = "kdsp_trriger";
    private static String JOB_GROUP_NAME = "kdsp_job";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止重复执行
        if (event.getApplicationContext().getParent() == null && count.incrementAndGet() <= 1) {
            initMyJob();
        }
    }

    public void initMyJob() {
//        //调度器
        Scheduler scheduler = null;
        try {
//            //创建scheduler
            scheduler = schedulerConfig.scheduler();

            String trigger = "";
            String job = "";
            String time = "";


            //任务：删除30天前的错误信息数据 每天执行一次
            trigger = "trigger_4";
            job = "job_4";
            time = "20 30 0 * * ?";
            createJob(scheduler, trigger, job, time, DeleteQuartzJobBean.class);


            //任务：每2分钟统计错误报警
            trigger = "trigger_7";
            job = "job_7";
            time = "0 0/5 * * * ?";
            createJob(scheduler, trigger, job, time, RealtimeStatisticsQuartzJobBean.class);

            //任务：每日统计前一天错误报警
            trigger = "trigger_8";
            job = "job_8";
            time = "0 0 8 * * ? ";
            createJob(scheduler, trigger, job, time, DayStatisticsQuartzJobBean.class);

            //任务：定时任务错误报警
            trigger = "trigger_9";
            job = "job_9";
            time = "0 0/5 * * * ?";
            createJob(scheduler, trigger, job, time, CheckSelfQuartzJobBean.class);

            scheduler.start();
        } catch (Exception e) {
            log.error("定时任务执行出错：", e);
        }
    }


    public void createJob(Scheduler scheduler, String stock_trigger, String stock_job, String stock_time, Class clazz_param) {
        try {
            TriggerKey triggerKey2 = TriggerKey.triggerKey(stock_trigger, TRIGGER_GROUP_NAME);
            CronTrigger trigger2 = (CronTrigger) scheduler.getTrigger(triggerKey2);
            if (null == trigger2) {
                Class clazz = clazz_param;
                //定义一个JobDetail,其中的定义Job类，是真正的执行逻辑所在
                JobDetail jobDetail2 = JobBuilder.newJob(clazz).withIdentity(stock_job, JOB_GROUP_NAME).build();
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(stock_time);
                //定义一个Trigger
                trigger2 = TriggerBuilder.newTrigger().withIdentity(stock_trigger, TRIGGER_GROUP_NAME)
                        .withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail2, trigger2);
                log.info("Quartz 创建了job:...:{}", jobDetail2.getKey());
            } else {
                log.info("job已存在:{}", trigger2.getKey());
            }
        } catch (Exception e) {
            log.error("创建任务报错，", e);
        }

    }

}
