package com.ark.monitor.bug.repoter.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.ark.monitor.bug.repoter.model.Project;
import com.ark.monitor.bug.repoter.enums.ProjectEnum;
import com.ark.monitor.bug.repoter.model.ServiceOwner;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
public class NacosConfigManager {
    private static Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

    private static ConfigService configService;

    private static ConfigService publicConfigService;

    private static NacosConfigManager nacosConfigManager = new NacosConfigManager();

    private static Boolean isProduction = null;

    private static String smsapikey = null;

	private static String nacosAddr;
	private static String nameSpace;
	private static final  String PRO ="pro";


    public String getConfig(String dataId, String group) {
        if (null != configService) {
            try {
                String value = configService.getConfig(dataId, group, 5000);
                if (StringUtils.isBlank(value)) {
                    //读取公共的命名空间下的配置
                    if (publicConfigService != null) {
                        return publicConfigService.getConfig(dataId, group, 5000);
                    }
                }
                configService.addListener(dataId, group, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        log.info(configInfo);
                    }
                });
                return value;
            } catch (Exception e) {
                log.error("异常：", e);
            }
        }
        return null;
    }


    public String getConfig(String dataId) {
        return getConfig(dataId, "DEFAULT_GROUP");
    }


    public static NacosConfigManager getInstance() {
        return nacosConfigManager;
    }

    @Value("${nacos.addr}")
    public void setNacosAddr(String nacosAddr) {
        this.nacosAddr = nacosAddr;
    }

    @Value("${spring.profiles.active}")
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public static String getNameSpace() {
        return nameSpace;
    }

    @PostConstruct
    public void initNacosConfigManager() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosAddr);
        try {
            //只有是测试环境才有这个逻辑，获取namespace 为public的命名空间，因为线上用的就是public命名空间
            if (!PRO.equals(nameSpace)) {
                publicConfigService = NacosFactory.createConfigService(properties);
                log.info("加载public下的配置");
                properties.put(PropertyKeyConst.NAMESPACE, nameSpace);
            }
            configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            log.error("异常：", e);
        }
    }

    public List<ServiceOwner> getOwnerList(String projectName){
        String serviceList = nacosConfigManager.getConfig("serviceList.properties");
        Map<String, List<ServiceOwner>> ownerMap = JSONObject.parseObject(serviceList,new TypeReference<Map<String,List<ServiceOwner>>>(){});
        String key = projectName.replaceAll("【","").replaceAll("】","");
        return ownerMap.get(key);
    }

    /**
     * 获取系统配置
     * @return
     */
    public List<Project> getProjectList(){
        String json = nacosConfigManager.getConfig("projectList.properties");
        List<Project> projects = JSONArray.parseArray(json, Project.class);
        if (CollectionUtils.isEmpty(projects)){
            projects = new ArrayList<>();
            for (ProjectEnum projectEnum:ProjectEnum.values()){
                Project project = new Project();
                project.setId(projectEnum.getId());
                project.setDesc(projectEnum.getDesc());
                projects.add(project);
            }
        }
        return projects;
    }


    /**
     * 获取相关配置
     */
    public static String getPropertiesByKey(String key,String defaultVal){
        Properties properties = new Properties();
        String content = nacosConfigManager.getConfig("application.properties");
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return properties.getProperty(key,defaultVal);
    }

}
