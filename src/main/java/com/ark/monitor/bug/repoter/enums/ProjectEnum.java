package com.ark.monitor.bug.repoter.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectEnum implements IEnum<Integer> {
    //1:某项目所有错误 2：某项目某分类 3：某项目某大分类下某小分类
    TEST(1, "【test-api】"),
    ;

    private Integer id;
    private String desc;

    @Override
    public Integer getValue() {
        return this.id;
    }


    private String desc() {
        return this.desc;
    }

}
