package com.ark.monitor.bug.repoter.error.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ark.monitor.bug.repoter.config.NacosConfigManager;
import com.ark.monitor.bug.repoter.enums.RealtimeStatusEnum;
import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.ark.monitor.bug.repoter.error.service.IErrorMsgService;
import com.ark.monitor.bug.repoter.model.DingTalkInteractiveCardCallBackPayload;
import com.ark.monitor.bug.repoter.model.DingTalkUser;
import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.ark.monitor.bug.repoter.statistics.service.IStatisticsRealtimeService;
import com.ark.monitor.bug.repoter.util.DingTalkInteractiveCardUtil;
import com.ark.monitor.bug.repoter.util.IpUtil;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 */
@Slf4j
@RestController
@RequestMapping("/ark/bug/repoter/error-msg")
public class ErrorMsgController {
    @Autowired
    private IErrorMsgService errorMsgService;
    @Autowired
    private IStatisticsRealtimeService iStatisticsRealtimeService;

    @Resource
    NacosConfigManager nacosConfigManager;

    static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(10, 30, 5L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "[钉钉log错误消息]");
                        }
                    }, new ThreadPoolExecutor.DiscardOldestPolicy());


    /**
     * 接收异常信息
     */
    @RequestMapping("/accept")
    public String accept(HttpServletRequest request, String project_name, String error_time, String msg, String env) {

        if (project_name == null || project_name.trim().length() == 0) {
            project_name = "other";
        }

        if (env == null || env.trim().length() == 0) {
            env = "pro";
        }

        String ip = IpUtil.getIpAdrress(request);
        if (StringUtils.isNotEmpty(ip)){
            //屏蔽测试环境或办公环境的IP
            if (ip.startsWith("172.16.") || ip.startsWith("192.168.")){
                return "ok";
            }
        }
        String finalEnv = env;
        String finalMsg = msg;
        String finalProjectName = project_name;
        log.info("insert msg:{}",msg);
        executor.execute(() -> {
            try {
                ErrorMsg errorMsg = new ErrorMsg();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime ldt = LocalDateTime.parse(error_time, df);
                errorMsg.setErrorTime(ldt);
                errorMsg.setProjectName(finalProjectName);
                errorMsg.setMsg(finalMsg);
                errorMsg.setEnv(finalEnv);
                errorMsg.setIp(ip);
                errorMsgService.save(errorMsg);
                log.info("插入数据成功");
                log.info(ip);
            } catch (Exception e) {
                log.error("插入数据异常，{},{}", finalMsg, e);
            }
        });
        return "ok";
    }

    /**
     * 修改异常处理信息
     */
    @RequestMapping(value = "/modify")
    public String  modify(@RequestBody DingTalkInteractiveCardCallBackPayload payload) {
            try {
                log.info("request={}", JSONObject.toJSONString(payload));
                DingTalkUser dingUser = DingTalkInteractiveCardUtil.memberMap.get(payload.getUserId());
                if(dingUser == null){
                    dingUser = DingTalkInteractiveCardUtil.getDingUser(payload.getUserId());
                    DingTalkInteractiveCardUtil.memberMap.put(payload.getUserId(),dingUser);
                }
                JSONObject jsonObject = (JSONObject) JSONObject.parse(payload.getValue());
                JSONObject cardPrivateData = (JSONObject) jsonObject.get("cardPrivateData");
                JSONObject params = (JSONObject) cardPrivateData.get("params");
                String statisticsId = params.getString("statisticsId");
                StatisticsRealtime realtime = iStatisticsRealtimeService.getById(Long.parseLong(statisticsId));
                if(realtime.getStatus().equals(RealtimeStatusEnum.Pending.getStatus()) || realtime.getStatus().equals(RealtimeStatusEnum.Processing.getStatus())){
                    realtime.setHandler(dingUser.getName());
                    realtime.setUpdateTime(LocalDateTime.now());
                    if(realtime.getStatus().equals(RealtimeStatusEnum.Pending.getStatus())){
                        realtime.setStatus(RealtimeStatusEnum.Processing.getStatus());
                    }else {
                        realtime.setStatus(RealtimeStatusEnum.Resolved.getStatus());
                    }
                    iStatisticsRealtimeService.updateById(realtime);
                    ErrorMsg errorMsg = errorMsgService.getById(realtime.getMsgId());
                    if (errorMsg == null){
                        return "errorMsg is null ";
                    }
                    DingTalkInteractiveCardUtil
                        .updateTemplateInteractiveCard(realtime,errorMsg,nacosConfigManager.getOwnerList(realtime.getProjectName()),payload.getOutTrackId());
                    log.info("更新任务状态成功");
                }
            } catch (Exception e) {
               log.error("插入数据异常", e);
            }
            return "ok";
    }

    /**
     * 展示异常详情
     */
    @RequestMapping("/detail")
    public void detail(HttpServletResponse response, Long id) {
        JSON.DEFAULT_GENERATE_FEATURE = SerializerFeature.config(
                JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.SkipTransientField, false);

        try {
            ErrorMsg errorMsg = errorMsgService.getById(id);
            if(errorMsg == null){
                return;
            }
            errorMsg.setShortMsg(errorMsg.getShortMsg());
            errorMsg.setTraceUrl(errorMsg.getTraceUrl());
            response.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            response.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(errorMsg, SerializerFeature.PrettyFormat,SerializerFeature.SortField.MapSortField));
            writer.flush();
            writer.close();
        } catch (Exception e) {
             log.error("查询异常信息数据异常，msgId :{},{}", id, e);
        }
    }
}

