package com.ark.monitor.bug.repoter.common.result;

import lombok.Data;

/**
 * API 调用返回的结果对象
 *
 */
@Data
public class Result<T> implements java.io.Serializable {

	/**
	 * 0成功,永远默认成功
	 */
    private String code = "0000";

    /**
    * 业务错误码
     */
    private String businessCode = ResultCode.SUCCESS.getCode();

    /**
     * 消息
     **/
    private String msg;

    /**
     * 详细消息
     **/
    private String detail;

    /**
     * 数据
     **/
    private T data;

    private boolean success;

    /**
     * @param resultCode
     */
    protected void setResultCode(ResultCode resultCode) {
        this.businessCode = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public Result() {
    }

    public Result(String code, String businessCode, String msg, String detail, T data, boolean success) {
        this.code = code;
        this.businessCode = businessCode;
        this.msg = msg;
        this.detail = detail;
        this.data = data;
        this.success = success;
    }

    public static Result success() {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        result.setData(new Object());
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static Result failure() {
        Result result = new Result();
        result.setResultCode(ResultCode.FAILURE);
        return result;
    }

    public static Result failure(String msg) {
        Result result = new Result();
        result.setBusinessCode(ResultCode.FAILURE.getCode());
        result.setMsg(msg);
        return result;
    }

    public static Result failure(ResultCode resultCode) {
        Result result = new Result();
        result.setResultCode(resultCode);
        return result;
    }

    public static Result failure(ResultCode resultCode, String detail) {
        Result result = new Result();
        result.setResultCode(resultCode);
        result.setDetail(detail);
        return result;
    }

    public boolean isSuccess() {
        return "0000".equals(code) && ResultCode.SUCCESS.getCode().equals(businessCode);
    }

    public static void main(String[] args) {
        System.out.println(Result.failure("异常了"));
    }
}
