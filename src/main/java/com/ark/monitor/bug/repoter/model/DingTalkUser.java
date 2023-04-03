package com.ark.monitor.bug.repoter.model;

import lombok.Data;

@Data
public class DingTalkUser {

    /**
     * 员工的userId
     */
    private String userid;
    /**
     * 员工在当前开发者企业账号范围内的唯一标识
     */
    private String unionid;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 手机号码
     */
    private String mobile;
}
