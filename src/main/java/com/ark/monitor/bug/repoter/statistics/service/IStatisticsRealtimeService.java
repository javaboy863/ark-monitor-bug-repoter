package com.ark.monitor.bug.repoter.statistics.service;

import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IStatisticsRealtimeService extends IService<StatisticsRealtime> {
    void sendYesterdayErrorStatistics() throws Exception;
}
