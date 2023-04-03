package com.ark.monitor.bug.repoter.error.service.impl;

import com.ark.monitor.bug.repoter.config.NacosConfigManager;
import com.ark.monitor.bug.repoter.enums.RealtimeStatusEnum;
import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.ark.monitor.bug.repoter.error.entity.QrtzTriggers;
import com.ark.monitor.bug.repoter.error.mapper.QrtzTriggersMapper;
import com.ark.monitor.bug.repoter.error.service.IQrtzTriggersService;
import com.ark.monitor.bug.repoter.model.ServiceOwner;
import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.ark.monitor.bug.repoter.util.DingTalkInteractiveCardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * job执行失败的播报
 * @ClassName QrtzTriggersServiceImpl
 * @Description
 */
@Slf4j
@Service
public class QrtzTriggersServiceImpl extends ServiceImpl<QrtzTriggersMapper, QrtzTriggers> implements IQrtzTriggersService {

    private static final String ERROR_LEVEL = "ERROR";
    private static final String OWNER_KEY = "【ark-monitor-bug-repoter】";

    private static final List<String> JOB_NAME_LIST = Arrays.asList("job_6", "job_7", "job_8");
    @Resource
    NacosConfigManager nacosConfigManager;

    @Override
    public void sendJobErrorMsg() {
        try {

            LambdaQueryWrapper lambdaQueryWrapper = new LambdaQueryWrapper<QrtzTriggers>()
                    .eq(QrtzTriggers::getTriggerState, ERROR_LEVEL)
                    .in(QrtzTriggers::getJobName, JOB_NAME_LIST);
            List<QrtzTriggers> qrtzTriggersList = this.list(lambdaQueryWrapper);
            if (CollectionUtils.isEmpty(qrtzTriggersList)) {
                return;
            }
            qrtzTriggersList.forEach(item -> {
                StatisticsRealtime realtime = new StatisticsRealtime();
                realtime.setId(1L);
                realtime.setStatus(RealtimeStatusEnum.Pending.getStatus());
                realtime.setProjectName(OWNER_KEY);
                realtime.setCreateTime(LocalDateTime.now());
                realtime.setErrorCount(1);
                ErrorMsg errorMsg = new ErrorMsg();
                String msg = item.getJobName() + "执行异常";
                errorMsg.setMsg(msg);
                List<ServiceOwner> ownerList = nacosConfigManager.getOwnerList(OWNER_KEY);
                DingTalkInteractiveCardUtil
                    .sendTemplateInteractiveCard(realtime, errorMsg, ownerList);
            });
        } catch (Exception e) {
            log.error("sendJobErrorMsg Exception e", e);
        }
    }

}
