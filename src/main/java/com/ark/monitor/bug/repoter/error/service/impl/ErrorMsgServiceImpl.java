package com.ark.monitor.bug.repoter.error.service.impl;

import com.ark.monitor.bug.repoter.config.NacosConfigManager;
import com.ark.monitor.bug.repoter.model.Project;
import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.ark.monitor.bug.repoter.error.mapper.ErrorMsgMapper;
import com.ark.monitor.bug.repoter.error.service.IErrorDesService;
import com.ark.monitor.bug.repoter.error.service.IErrorMsgService;
import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.ark.monitor.bug.repoter.statistics.service.IStatisticsRealtimeService;
import com.ark.monitor.bug.repoter.util.DingTalkInteractiveCardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class ErrorMsgServiceImpl extends ServiceImpl<ErrorMsgMapper, ErrorMsg> implements IErrorMsgService {
    @Autowired
    private IErrorDesService errorDesService;
    @Resource
    ErrorMsgMapper errorMsgMapper;
    @Resource
    NacosConfigManager nacosConfigManager;
    @Resource
    private IStatisticsRealtimeService statisticsRealtimeService;

    private static final int PAGE_SIZE = 500;



    /*
     * 获取每日 各系统 错误数错误
     * */
    public List getErrorCnt() {
        String sql = "select project_name,count(1) cnt from t_error_msg where  error_time >CAST((CAST(SYSDATE()AS DATE) - INTERVAL 1 DAY)AS DATETIME)\n" +
                "and error_time< CAST(CAST(SYSDATE()AS DATE)AS DATETIME)  and env='pro' \n" +
                "group by project_name";
        List list = errorMsgMapper.execSql_Simple(sql);
        return list;
    }

    /*
     * 获取每日 各系统 各分类下错误数据统计
     * */
    public List getErrorGroupCnt() {
        String sql = "select t1.project_name,t2.error_type,count(1) cnt from t_error_msg t1 inner join \n" +
                "t_error_des t2\n" +
                "on t1.id = t2.error_id\n" +
                "where  t1.error_time >CAST((CAST(SYSDATE()AS DATE) - INTERVAL 1 DAY)AS DATETIME)\n" +
                "and t1.error_time< CAST(CAST(SYSDATE()AS DATE)AS DATETIME)\n" +
                "group by t1.project_name,t2.error_type\n" +
                "\n";
        List list = errorMsgMapper.execSql_Simple(sql);
        return list;
    }

    /**
     * 删除某个日期之前的数据
     */
    public void delInfo(LocalDateTime time) {
        this.remove(Wrappers.lambdaQuery(ErrorMsg.class)
                .le(ErrorMsg::getErrorTime, time));
    }


    @Override
    public void statisticsErrorMsg() {
        String recentlyMinusMinutes = NacosConfigManager
            .getPropertiesByKey("realtime.statistics.recently.minusMinutes","50000");
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusMinutes(Integer.valueOf(recentlyMinusMinutes));
            List<Project> projects = nacosConfigManager.getProjectList();
            for (Project project : projects) {
                if (project.getId() <= 4) {
                    continue;
                }
                LambdaQueryWrapper<ErrorMsg> queryWrapper = Wrappers.lambdaQuery(ErrorMsg.class)
                        .eq(ErrorMsg::getProjectName, project.getDesc())
                        .ge(ErrorMsg::getCreatedAt, startTime)
                        .lt(ErrorMsg::getCreatedAt, endTime)
                        .eq(ErrorMsg::getEnv, "pro");
                List<ErrorMsg> errorMsgList = this.baseMapper.selectList(queryWrapper);
                if (!CollectionUtils.isEmpty(errorMsgList)) {
                    Map<String, List<ErrorMsg>> errorMap = errorMsgList.stream().collect(Collectors.groupingBy(ErrorMsg::getShortMsg));
                    for (Map.Entry<String, List<ErrorMsg>> entry : errorMap.entrySet()) {
                        try {
                            ErrorMsg errorMsg = entry.getValue().get(0);
                            if (errorMsg == null){
                                continue;
                            }
                            List<ErrorMsg> errorList = entry.getValue();
                            StatisticsRealtime realtime = new StatisticsRealtime();
                            realtime.setMsgId(errorMsg.getId());
                            realtime.setStatus(0);
                            realtime.setProjectName(project.getDesc());
                            realtime.setCreateTime(endTime);

                            realtime.setErrorCount(errorList.size());
                            statisticsRealtimeService.save(realtime);
                            DingTalkInteractiveCardUtil
                                .sendTemplateInteractiveCard(realtime, errorMsg, nacosConfigManager.getOwnerList(realtime.getProjectName()));
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("每5分钟超过一千条异常", e);
        }
    }


}

