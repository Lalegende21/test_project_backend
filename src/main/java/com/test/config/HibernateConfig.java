package com.test.config;

import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer() {
        return (hibernateProperties) -> hibernateProperties.put("hibernate.type.json_format_mapper", "jackson");
    }
}
