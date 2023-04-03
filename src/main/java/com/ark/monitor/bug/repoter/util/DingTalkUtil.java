package com.ark.monitor.bug.repoter.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.ark.monitor.bug.repoter.constant.TokenConstant;
import com.google.common.collect.Maps;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

@Slf4j
public class DingTalkUtil {

    private static final String DINGDING_URL = "https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%s&sign=%s";



    /**
     * @param content    内容
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    public static String sendMsgDefault(String content) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return buildReqStr(content, TokenConstant.DEFAULT_Secret, TokenConstant.DEFAULT_AccessToken,false, null);
    }

    /**
     * @param content    内容
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    public static String sendMsgDefault(String content, String dingdingSecret, String dingdingAccessToken) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return buildReqStr(content, dingdingSecret, dingdingAccessToken,false, null);
    }

    /**
     * 给钉钉群发送消息方法
     *
     * @param content 消息内容
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String sendMsg(String content, String dingdingSecret, String dingdingAccessToken) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        try {
            //群机器人复制到的秘钥secret
            String secret = dingdingSecret;
            //获取系统时间戳
            long timestamp = System.currentTimeMillis();
            //拼接
            String stringToSign = timestamp + "\n" + secret;
            //使用HmacSHA256算法计算签名
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            //进行Base64 encode 得到最后的sign，可以拼接进url里
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            //钉钉机器人地址（配置机器人的webhook）
            String dingUrl = String.format(DINGDING_URL, dingdingAccessToken, timestamp, sign);

            String result = HttpUtil.post(dingUrl, content);
            return result;
        } catch (Exception e) {
//             log.error("钉钉推送消息出现异常");
            e.printStackTrace();
            return null;
        }

    }


    /**
     * @param content    内容
     * @param isAtAll    是否@所有人 如果写true mobileList失效
     * @param mobileList @人的手机号
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    public static String buildReqStr(String content, String dingdingSecret, String dingdingAccessToken, boolean isAtAll, List<String> mobileList) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);
        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);
        String contens = JSON.toJSONString(reqMap);

        String result = sendMsg(contens, dingdingSecret, dingdingAccessToken);
        return result;
    }

}
