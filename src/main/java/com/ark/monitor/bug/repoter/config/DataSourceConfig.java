package com.ark.monitor.bug.repoter.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 */
@Configuration
@MapperScan(basePackages = "com.ark.**.mapper",
        sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("${datasource.data.mysql.jdbc-url}")
    private String jdbcUrl;
    @Value("${datasource.data.mysql.password}")
    private String password;
    @Value("${datasource.data.mysql.user}")
    private String user;
    @Value("${datasource.data.mysql.max-pool-size}")
    private Integer maxPoolSize;

    @Bean(name = "baseDataSource")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setPassword(password);
        config.setUsername(user);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(20);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        return globalConfig;
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("baseDataSource") DataSource dataSource, GlobalConfig globalConfig,
                                               PaginationInterceptor paginationInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSource);
        mybatisSqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/**/*.xml"));
        mybatisSqlSessionFactoryBean.setTypeAliasesPackage("com.ark.**.entity");
        mybatisSqlSessionFactoryBean.setTypeEnumsPackage("com.ark.**.enums");
        mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);
        mybatisSqlSessionFactoryBean.setPlugins(paginationInterceptor);
        mybatisSqlSessionFactoryBean.getObject()
                .getConfiguration()
                .setMapUnderscoreToCamelCase(true);

        return mybatisSqlSessionFactoryBean.getObject();
    }

}
