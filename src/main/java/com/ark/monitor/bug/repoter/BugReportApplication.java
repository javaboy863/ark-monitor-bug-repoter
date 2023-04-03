package com.ark.monitor.bug.repoter;

import com.ark.monitor.bug.repoter.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.ark.*"})
@SpringBootApplication
public class BugReportApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BugReportApplication.class, args);
        SpringContextUtil.setApplicationContext(context);
    }

}
