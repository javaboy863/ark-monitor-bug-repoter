package com.ark.monitor.bug.repoter.common.result;

/**
 * API调用结果状态码定义
 */
public enum ResultCode {
    /**
     * 响应[消息中心]专用
     */

    SUCCESS("0000", "成功"),
    FAILURE("0001", "失败"),



    ;


    private String code;

    private String msg;


    private ResultCode(String code, String msg) {

        this.code = code;
        this.msg = msg;
    }


    public String getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

}
