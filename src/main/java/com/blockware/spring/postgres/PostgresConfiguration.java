package com.blockware.spring.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class PostgresConfiguration {

    @Value("${postgres.jdbc_url:#{null}}")
    private String jdbcURL;

    @Value("${postgres.jdbc_user:postgres}")
    private String jdbcUsername;

    @Value("${postgres.jdbc_pass:#{null}}")
    private String jdbcPassword;

}
