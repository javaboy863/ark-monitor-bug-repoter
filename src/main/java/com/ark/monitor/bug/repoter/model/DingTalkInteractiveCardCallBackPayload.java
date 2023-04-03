package com.ark.monitor.bug.repoter.model;

import lombok.Data;

@Data
public class DingTalkInteractiveCardCallBackPayload {
    /**
     * 动态卡片ID
     */
    private String outTrackId;
    private String corpId;
    /**
     * 用户ID
     */
    private String userId;
    private String value;
    private String content;
    /**
     * 报警信息ID
     */
    private String statisticsId;
}
