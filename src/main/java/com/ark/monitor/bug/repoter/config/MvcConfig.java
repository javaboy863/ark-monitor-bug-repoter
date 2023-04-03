package com.ark.monitor.bug.repoter.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;

/**
 * 全局 spring mvc 的配置
 */
@Configuration
public class MvcConfig {
    /**
     * jackson
     *
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder.json();
        jackson2ObjectMapperBuilder.indentOutput(false)
                                   .serializers(new LocalDateTimeSerializer(localDateTime), new LocalDateSerializer(localDate))
                                   .deserializers(new LocalDateTimeDeserializer(localDateTime), new LocalDateDeserializer(localDate));
        return jackson2ObjectMapperBuilder;
    }

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.serializationInclusion(Include.NON_NULL)
                                                               .build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }
}
