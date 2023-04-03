package com.ark.monitor.bug.repoter.util;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MINUTE_PATTERN;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.dingtalkim_1_0.Client;
import com.aliyun.dingtalkim_1_0.models.BatchQueryGroupMemberHeaders;
import com.aliyun.dingtalkim_1_0.models.BatchQueryGroupMemberRequest;
import com.aliyun.dingtalkim_1_0.models.BatchQueryGroupMemberResponse;
import com.aliyun.dingtalkim_1_0.models.SendInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.SendInteractiveCardRequest;
import com.aliyun.dingtalkim_1_0.models.SendInteractiveCardResponse;
import com.aliyun.dingtalkim_1_0.models.UpdateInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.UpdateInteractiveCardRequest;
import com.aliyun.dingtalkim_1_0.models.UpdateInteractiveCardResponse;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaPair;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.ark.monitor.bug.repoter.config.NacosConfigManager;
import com.ark.monitor.bug.repoter.enums.RealtimeStatusEnum;
import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.ark.monitor.bug.repoter.model.DingTalkUser;
import com.ark.monitor.bug.repoter.model.ServiceOwner;
import com.ark.monitor.bug.repoter.statistics.entity.StatisticsRealtime;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiImChatScencegroupMessageSendV2Request;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiImChatScencegroupMessageSendV2Response;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.taobao.api.ApiException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class DingTalkInteractiveCardUtil {

    private static final String PRO = "pro";
    private static final LoadingCache<String, AccessToken> localCache = CacheBuilder.newBuilder().initialCapacity(5).maximumSize(5)
            .expireAfterWrite(60, TimeUnit.MINUTES).refreshAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<String, AccessToken>() {
        @Override
        //默认的数据加载实现,当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载
        public AccessToken load(String s) {
            return getAccessTokenBy();
        }
    });
    private static final LoadingCache<String, Client> clientCache = CacheBuilder.newBuilder().initialCapacity(5).maximumSize(5).expireAfterWrite(60, TimeUnit.MINUTES).build(new CacheLoader<String, Client>() {
        @Override
        public Client load(String s) throws Exception {
            return createClient();
        }
    });
    public static final String ROBOT_CODE = "";
    public static final String TEST_ROBOT_CODE = "";

    public static final String OPEN_CONVERSATION_ID = "";
    public static final String TEST_OPEN_CONVERSATION_ID = "";

    public static final String CARD_TEMPLATE_ID = "";
    public static final String TEST_CARD_TEMPLATE_ID = "";

    public static final String APP_KEY = "";

    public static final String APP_SECRET = "";
    /**
     * 测试环境回调地址
     */
    public static final String CallbackRouteKey_DEV = "modify_callback_dev";
    /**
     * 生产环境回调地址
     */
    public static final String CallbackRouteKey_PRO = "modify_callback_pro";

    public static final Map<String, DingTalkUser> memberMap = new HashMap<>();


    static {
        try {
            List<String> userIds = getMembers();
            if (!CollectionUtils.isEmpty(userIds)) {
                for (String userId : userIds) {
                    DingTalkUser dingUser = getDingUser(userId);
                    if (dingUser != null) {
                        memberMap.put(dingUser.getName(), dingUser);
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static String getAccessToken() {
        try {
            AccessToken accessToken = localCache.get("token");
            if(accessToken.isExpired()){
                log.info("accessToken expires at: {}", JSONObject.toJSONString(accessToken.getExpiredAt()));
                localCache.refresh("token");
            }
            return localCache.get("token").getAccessToken();
        } catch (Exception e) {
            log.error("", e);
        }
        return "";
    }

    /**
     * 获取企业内部应用访问token
     *
     * @return
     */
    public static AccessToken getAccessTokenBy() {
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setHttpMethod("GET");
            //appkey
            req.setAppkey(APP_KEY);
            //appsecret
            req.setAppsecret(APP_SECRET);
            OapiGettokenResponse response = client.execute(req);
            log.info("获取企业内部应用response={}", JSONObject.toJSONString(response));
            return new AccessToken(response.getAccessToken(),LocalDateTime.now().plusSeconds(response.getExpiresIn() - 600));
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用 Token 初始化账号Client
     *
     * @return Client
     * @throws Exception
     */
    public static Client createClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }

    public static Client getClient() throws ExecutionException {
        return clientCache.get("client");
    }

    /**
     * 发送互动卡片
     *
     * @throws Exception
     */
    public static void sendTemplateInteractiveCard(StatisticsRealtime statisticsRealtime,
        ErrorMsg errorMsg, List<ServiceOwner> ownerList) {
        log.info("statisticsRealtime={}", JSONObject.toJSONString(statisticsRealtime));
        try {
            List<DingTalkUser> userList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ownerList)) {
                for (ServiceOwner owner : ownerList) {
                    userList.add(memberMap.get(owner.getName()));
                }
            }
            Map<String, String> atOpenIds = new HashMap<>();
            if (!CollectionUtils.isEmpty(userList)) {
                for (DingTalkUser dingUser : userList) {
                    atOpenIds.put(dingUser.getUserid(), dingUser.getName());
                }
            }
            Client client = getClient();
            SendInteractiveCardHeaders sendInteractiveCardHeaders = new SendInteractiveCardHeaders();
            sendInteractiveCardHeaders.xAcsDingtalkAccessToken = getAccessToken();
            Map<String, String> cardDataCardParamMap = setCardDataCardParamMap(statisticsRealtime, errorMsg, ownerList);
            SendInteractiveCardRequest.SendInteractiveCardRequestCardData cardData = new SendInteractiveCardRequest.SendInteractiveCardRequestCardData()
                    .setCardParamMap(cardDataCardParamMap);
            SendInteractiveCardRequest sendInteractiveCardRequest = new SendInteractiveCardRequest();
                if (isProductEnv()) {
                    sendInteractiveCardRequest.setCardTemplateId(CARD_TEMPLATE_ID);
                } else {
                    sendInteractiveCardRequest.setCardTemplateId(TEST_CARD_TEMPLATE_ID);
                }
                if (isProductEnv()) {
                    sendInteractiveCardRequest.setOpenConversationId(OPEN_CONVERSATION_ID);
                } else {
                    sendInteractiveCardRequest.setOpenConversationId(TEST_OPEN_CONVERSATION_ID);
                }
            if (isProductEnv()) {
                sendInteractiveCardRequest.setRobotCode(ROBOT_CODE);
            } else {
                sendInteractiveCardRequest.setRobotCode(TEST_ROBOT_CODE);
            }
            if (isProductEnv()) {
                sendInteractiveCardRequest.setCallbackRouteKey(CallbackRouteKey_PRO);
            } else {
                sendInteractiveCardRequest.setCallbackRouteKey(CallbackRouteKey_DEV);
            }
            sendInteractiveCardRequest.setOutTrackId(UUID.randomUUID().toString());
            sendInteractiveCardRequest.setConversationType(1);
            sendInteractiveCardRequest.setCardData(cardData).setPullStrategy(false);
            sendInteractiveCardRequest.setAtOpenIds(atOpenIds);
            log.info("request={}", JSONObject.toJSONString(sendInteractiveCardRequest));
            SendInteractiveCardResponse response = client.sendInteractiveCardWithOptions(sendInteractiveCardRequest, sendInteractiveCardHeaders, new RuntimeOptions());
            log.info("response={}", JSONObject.toJSONString(response.getBody()));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private static boolean isProductEnv() {
        return NacosConfigManager.getNameSpace().equals(PRO);
    }

    /**
     * 更新互动卡片
     *
     * @param statisticsRealtime
     * @param errorMsg
     * @param ownerList
     * @param outTrackId
     * @throws Exception
     */
    public static void updateTemplateInteractiveCard(StatisticsRealtime statisticsRealtime, ErrorMsg errorMsg, List<ServiceOwner> ownerList, String outTrackId) throws Exception {
        Client client = getClient();
        UpdateInteractiveCardHeaders updateInteractiveCardHeaders = new UpdateInteractiveCardHeaders();
        updateInteractiveCardHeaders.xAcsDingtalkAccessToken = getAccessToken();
        UpdateInteractiveCardRequest.UpdateInteractiveCardRequestCardOptions cardOptions = new UpdateInteractiveCardRequest.UpdateInteractiveCardRequestCardOptions()
                .setUpdateCardDataByKey(false);
        java.util.Map<String, String> cardDataCardParamMap = setCardDataCardParamMap(statisticsRealtime, errorMsg, ownerList);
        UpdateInteractiveCardRequest.UpdateInteractiveCardRequestCardData cardData = new UpdateInteractiveCardRequest.UpdateInteractiveCardRequestCardData()
                .setCardParamMap(cardDataCardParamMap);
        UpdateInteractiveCardRequest updateInteractiveCardRequest = new UpdateInteractiveCardRequest()
                .setOutTrackId(outTrackId)
                .setCardData(cardData)
                .setUserIdType(1)
                .setCardOptions(cardOptions);
        try {
            UpdateInteractiveCardResponse response = client.updateInteractiveCardWithOptions(updateInteractiveCardRequest, updateInteractiveCardHeaders, new RuntimeOptions());
            log.info("response={}", JSONObject.toJSONString(response.getBody()));
        } catch (Exception _err) {
            log.error("", _err);
        }
    }

    /**
     * 获取用户详情
     *
     * @param userId
     * @return
     */
    public static DingTalkUser getDingUser(String userId) {
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
            OapiV2UserGetRequest req = new OapiV2UserGetRequest();
            req.setUserid(userId);
            OapiV2UserGetResponse response = client.execute(req, getAccessToken());
            log.info("response={}", JSONObject.toJSONString(response.getBody()));
            DingTalkUser dingUser = new DingTalkUser();
            OapiV2UserGetResponse.UserGetResponse pageResult = response.getResult();
            BeanUtil.copyProperties(pageResult, dingUser);
            return dingUser;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取群成员
     *
     * @throws Exception
     */
    public static List<String> getMembers() throws Exception {
        Client client = getClient();
        BatchQueryGroupMemberHeaders batchQueryGroupMemberHeaders = new BatchQueryGroupMemberHeaders();
        batchQueryGroupMemberHeaders.xAcsDingtalkAccessToken = getAccessToken();
        BatchQueryGroupMemberRequest batchQueryGroupMemberRequest = new BatchQueryGroupMemberRequest();
        try {
            batchQueryGroupMemberRequest.setOpenConversationId(OPEN_CONVERSATION_ID);
            batchQueryGroupMemberRequest.setMaxResults(100L);
            BatchQueryGroupMemberResponse response = client.batchQueryGroupMemberWithOptions(batchQueryGroupMemberRequest, batchQueryGroupMemberHeaders, new com.aliyun.teautil.models.RuntimeOptions());
            log.info("response={}", JSONObject.toJSONString(response.getBody()));
            return response.getBody().memberUserIds;
        } catch (Exception _err) {
            log.error("", _err);
        }
        return null;
    }

    /**
     * 发送每日统计
     *
     * @param statisticsMap
     * @throws Exception
     */
    public static void sendErrorStatisticsPerDay(Map<String, List<StatisticsRealtime>> statisticsMap) throws Exception {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2");
        OapiImChatScencegroupMessageSendV2Request request = new OapiImChatScencegroupMessageSendV2Request();
        request.setTargetOpenConversationId(OPEN_CONVERSATION_ID);
        request.setMsgTemplateId("inner_app_template_markdown");
        request.setRobotCode(ROBOT_CODE);
        Map<String, String> map = new HashMap<>();
        OapiRobotSendRequest.Markdown markdown = getMarkdown(statisticsMap);
        map.put("title", markdown.getTitle());
        map.put("markdown_content", markdown.getText());
        request.setMsgParamMap(JSONObject.toJSONString(map));
        try {
            OapiImChatScencegroupMessageSendV2Response rsp = client.execute(request, getAccessToken());
            log.info("x rsp={}", JSONObject.toJSONString(rsp));
        } catch (Exception _err) {
            log.error("sendErrorStatisticsPerDay error", _err);
        }
    }

    /**
     * 获取每日统计markdown
     *
     * @param statisticsMap
     * @return
     */
    private static OapiRobotSendRequest.Markdown getMarkdown(Map<String, List<StatisticsRealtime>> statisticsMap) {
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("异常报警信息统计");
        StringBuilder sb = new StringBuilder();
        DateTime startDateTime = DateUtil.beginOfDay(DateUtil.yesterday());
        DateTime endDateTime = DateUtil.beginOfDay(new Date());
        sb.append("#### 统计时间：").append(DateUtil.format(startDateTime, NORM_DATETIME_MINUTE_PATTERN))
                .append("～").append(DateUtil.format(endDateTime, NORM_DATETIME_MINUTE_PATTERN)).append("\n").append("----------------------- \n\n");
        NacosConfigManager nacosConfigManager = SpringContextUtil.getBean(NacosConfigManager.class);
        for (Map.Entry<String, List<StatisticsRealtime>> entry : statisticsMap.entrySet()) {
            List<ServiceOwner> ownerList = nacosConfigManager.getOwnerList(entry.getKey());
            log.info("sendErrorStatisticsPerDay ownerList {}", JSONObject.toJSONString(ownerList));
            String owner = null;
            if (!CollectionUtils.isEmpty(ownerList)) {
                owner = ownerList.stream().map(ServiceOwner::getName).collect(Collectors.joining(","));
            }
            List<StatisticsRealtime> statisticsInfoList = entry.getValue();
            long total = statisticsInfoList.stream().mapToLong(StatisticsRealtime::getErrorCount).sum();
            long pending = statisticsInfoList.stream().filter(x -> x.getStatus().equals(0)).mapToLong(StatisticsRealtime::getErrorCount).sum();
            long processing = statisticsInfoList.stream().filter(x -> x.getStatus().equals(1)).mapToLong(StatisticsRealtime::getErrorCount).sum();
            long resolved = statisticsInfoList.stream().filter(x -> x.getStatus().equals(2)).mapToLong(StatisticsRealtime::getErrorCount).sum();
            long ignore = statisticsInfoList.stream().filter(x -> x.getStatus().equals(3)).mapToLong(StatisticsRealtime::getErrorCount).sum();
            sb.append("#### 项目名称：").append(entry.getKey()).append(" ，项目负责人：【").append(owner).append("】\n")
                    .append("#### 异常总次数：").append(total).append("      ｜ 未认领：").append(pending).append("      ｜ 已认领：").append(processing).append("      ｜ 已解决：").append(resolved).append("      ｜ 已忽略：").append(ignore).append(" \n")
                    .append("----------------------- \n\n");
        }
        markdown.setText(sb.toString());
        return markdown;
    }


    /**
     * 组装互动卡片请求参数
     *
     * @param statisticsRealtime
     * @param ownerList
     * @return
     */
    public static Map<String, String> setCardDataCardParamMap(StatisticsRealtime statisticsRealtime, ErrorMsg errorMsg, List<ServiceOwner> ownerList) {
        String url = "";
        if (isProductEnv()) {
            url = "https://.com/error-msg/detail?id=" + statisticsRealtime.getMsgId();
        } else {
            url = "https://.net/error-msg/detail?id=" + statisticsRealtime.getMsgId();
        }
        String owners = "";
        if (!CollectionUtils.isEmpty(ownerList)) {
            List<String> nameList = ownerList.stream().map(ServiceOwner::getName).collect(Collectors.toList());
            owners = String.join(",", nameList);
        }
        String traceUrl =errorMsg.getTraceUrl();
        String msg = errorMsg.getShortMsg();
        if (msg.indexOf(statisticsRealtime.getProjectName()) != -1){
            msg = msg.replace(statisticsRealtime.getProjectName(), "");
        }
        if (msg.indexOf("---") != -1){
            msg = msg.replace("---", "");
        }
        TeaPair projectName =  new TeaPair("projectName", statisticsRealtime.getProjectName());
        TeaPair errorCount  =  new TeaPair("errorCount", statisticsRealtime.getErrorCount().toString());
        TeaPair errorMsgPair = new TeaPair("errorMsg", msg);
        TeaPair detailUrl = new TeaPair("detailUrl", url);
        TeaPair ownersPair =new TeaPair("owners", owners);
        TeaPair status =new TeaPair("status", RealtimeStatusEnum.getByStatus(statisticsRealtime.getStatus()).getDesc());
        TeaPair handler = new TeaPair("handler", statisticsRealtime.getHandler());
        TeaPair statisticsId = new TeaPair("statisticsId", statisticsRealtime.getId().toString());
        TeaPair buttonDesc = new TeaPair("buttonDesc", RealtimeStatusEnum.getByStatus(statisticsRealtime.getStatus()).getButtonDesc());
        Map<String, String> stringObjectMap = null;
        if (StringUtils.isNotEmpty(traceUrl)){
            stringObjectMap = TeaConverter.buildMap(
                    projectName,errorCount,errorMsgPair,detailUrl,ownersPair,status,handler,statisticsId,buttonDesc,
                    new TeaPair("traceUrl", traceUrl)
            );
        }else {
            stringObjectMap = TeaConverter.buildMap(
                    projectName,errorCount,errorMsgPair,detailUrl,ownersPair,status,handler,statisticsId,buttonDesc
            );
        }

        return stringObjectMap;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @Slf4j
    static class AccessToken{
        private String accessToken;

        private LocalDateTime expiredAt;

        public boolean isExpired() {
            return this.expiredAt.compareTo(LocalDateTime.now()) <= 0;
        }
    }
}
