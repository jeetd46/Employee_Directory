package com.api.employeeDirectory.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppProperties.class, DirectoryProperties.class, PaginationProperties.class})
public class PropertiesConfiguration {
}
