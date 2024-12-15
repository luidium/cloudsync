package com.luidium.cloudsync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Autowired
    private DatabaseProperties databaseProperties;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(databaseProperties.getUrl())
                .username(databaseProperties.getUsername())
                .password(databaseProperties.getPassword())
                .build();
    }
}
