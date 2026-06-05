package com.hr.workwave.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(
            DataSource dataSource,
            @Value("${spring.liquibase.change-log:classpath:/db/changelog/db.changelog-master.xml}") String changeLog,
            @Value("${spring.liquibase.enabled:true}") boolean enabled,
            @Value("${spring.liquibase.default-schema:}") String defaultSchema,
            @Value("${spring.liquibase.contexts:}") String contexts,
            @Value("${spring.liquibase.labels:}") String labels
    ) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);

        if (!defaultSchema.isBlank()) {
            liquibase.setDefaultSchema(defaultSchema);
        }
        if (!contexts.isBlank()) {
            liquibase.setContexts(contexts);
        }
        if (!labels.isBlank()) {
            liquibase.setLabelFilter(labels);
        }

        return liquibase;
    }
}

