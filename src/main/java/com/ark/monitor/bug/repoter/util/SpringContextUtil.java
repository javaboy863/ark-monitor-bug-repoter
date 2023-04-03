package com.ark.monitor.bug.repoter.util;

import org.springframework.context.ApplicationContext;

/**
 * @ClassName SpringContextUtil
 * @Description
 */
public class SpringContextUtil {

    public static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}
