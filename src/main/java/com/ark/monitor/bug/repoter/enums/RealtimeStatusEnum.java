package com.ark.monitor.bug.repoter.enums;

import lombok.Getter;

/**
 *
 */
@Getter
public enum RealtimeStatusEnum {
    Pending(0,"待处理","我来跟进"),
    Processing(1,"处理中","处理完成"),
    Resolved(2,"已解决","已处理"),
    Ignore(3,"已忽略","已忽略");
    private final Integer status;

    private final String desc;

    private final String buttonDesc;

    RealtimeStatusEnum(Integer status, String desc,String buttonDesc) {
        this.status = status;
        this.desc = desc;
        this.buttonDesc = buttonDesc;
    }

    public static RealtimeStatusEnum getByStatus(Integer status){
        for(RealtimeStatusEnum statusEnum:RealtimeStatusEnum.values()){
            if(statusEnum.getStatus() == status){
                return statusEnum;
            }
        }
        return null;
    }
}
