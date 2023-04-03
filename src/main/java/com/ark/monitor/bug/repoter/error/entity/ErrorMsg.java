package com.ark.monitor.bug.repoter.error.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.ark.monitor.bug.repoter.config.NacosConfigManager;
import com.ark.monitor.bug.repoter.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_error_msg")
@Slf4j
@JSONType(orders={"projectName","shortMsg","traceUrl","errorTime","createdAt","ip","id","errorTime","env","status","msg"})
public class ErrorMsg extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 是否处理
     */
    private Integer status;

    /**
     * 业务报错时间
     */
    private LocalDateTime errorTime;
    /**
     * 运行环境
     */
    private String env;
    /**
     * IP
     */
    private String ip;

    /**
     * shortMsg
     */
    private transient String shortMsg;
    private transient String traceUrl;

    public String getTraceUrl() {
        if (StringUtils.isNotEmpty(traceUrl)){
            return traceUrl;
        }
        if (StringUtils.isEmpty(msg)){
            return StringUtils.EMPTY;
        }
        String regex = NacosConfigManager.getPropertiesByKey("short.exception.pinpoint.regex","https://pinpoint.q-gp.com/transactionDetail\\?transactionInfo.+]");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.msg);
        if (matcher.find()) {
            traceUrl = matcher.group(0).replace("]","");
            return traceUrl;
        }
        return StringUtils.EMPTY;
    }

    public void setShortMsg(String shortMsg) {
        this.shortMsg = shortMsg;
    }

    public String getShortMsg() {
        if (StringUtils.isNotEmpty(shortMsg)){
            return shortMsg;
        }
        if (StringUtils.isEmpty(msg)){
            return StringUtils.EMPTY;
        }
        String regex = NacosConfigManager.getPropertiesByKey("short.exception.msg.regex","([a-zA-Z]+\\.){1,}[a-zA-Z]+.[a-zA-Z]+Exception:.+");
        Pattern pattern = Pattern.compile(regex);
        String msg = this.getMsg();
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()){
            this.shortMsg = matcher.group(0).trim();
        }else {
            this.shortMsg = msg.trim();
        }
        return shortMsg;
    }

}
