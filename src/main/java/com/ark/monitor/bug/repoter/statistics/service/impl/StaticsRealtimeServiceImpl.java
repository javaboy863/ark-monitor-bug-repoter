package com.ark.monitor.bug.repoter.statistics.service.impl;

import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.ark.monitor.bug.repoter.statistics.mapper.StatisticsRealtimeMapper;
import com.ark.monitor.bug.repoter.statistics.service.IStatisticsRealtimeService;
import com.ark.monitor.bug.repoter.util.DingTalkInteractiveCardUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class StaticsRealtimeServiceImpl extends ServiceImpl<StatisticsRealtimeMapper, StatisticsRealtime> implements
    IStatisticsRealtimeService {
    @Override
    public void sendYesterdayErrorStatistics() throws Exception {
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        log.info("executeYesterdayStatistics  startDateTime {} ,endDateTime{}  ", startDateTime, endDateTime);

        LambdaQueryWrapper<StatisticsRealtime> queryWrapper = Wrappers.lambdaQuery(StatisticsRealtime.class)
                .ge(StatisticsRealtime::getCreateTime, startDateTime)
                .lt(StatisticsRealtime::getCreateTime, endDateTime)
                .orderByDesc();
        List<StatisticsRealtime> statisticsList = this.baseMapper.selectList(queryWrapper);
        log.info("executeYesterdayStatistics statisticsList={}", JSONObject.toJSONString(statisticsList));
        if (!CollectionUtils.isEmpty(statisticsList)) {
            Map<String, List<StatisticsRealtime>> statisticsMap = statisticsList.stream().collect(Collectors.groupingBy(StatisticsRealtime::getProjectName));
            DingTalkInteractiveCardUtil.sendErrorStatisticsPerDay(statisticsMap);
        }
    }



}
