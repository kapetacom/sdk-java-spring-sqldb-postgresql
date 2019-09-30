package com.blockware.spring.postgres;

import com.blockware.spring.cluster.BlockwareClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Slf4j
abstract public class AbstractPostgresConfig {
    private static final String RESOURCE_TYPE = "sqldb.blockware.com/v1/postgresql";

    private static final String PORT_TYPE = "postgres";

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private BlockwareClusterService blockwareClusterService;

    private final String resourceName;

    public AbstractPostgresConfig(String resourceName) {
        this.resourceName = resourceName;
    }

    @Bean
    public DataSource dataSource() {

        final BlockwareClusterService.ResourceInfo info = blockwareClusterService.getResourceInfo(RESOURCE_TYPE, PORT_TYPE, resourceName);
        Optional<String> dbUsername = Optional.ofNullable(info.getCredentials().get("username"));
        Optional<String> dbPassword = Optional.ofNullable(info.getCredentials().get("password"));

        String databaseName = String.valueOf(info.getOptions().getOrDefault("dbName", resourceName));

        String jdbcBaseUrl = String.format("jdbc:postgresql://%s:%s", info.getHost(), info.getPort());

        log.info("Connecting to postgres server: {} for database: {}", jdbcBaseUrl, databaseName);

        String jdbcUrl = String.format("%s/%s", jdbcBaseUrl, databaseName);

        final DataSourceBuilder<?> builder = DataSourceBuilder.create();

        builder.url(jdbcUrl);

        dbUsername.ifPresent(s -> builder.username(s)
                .password(dbPassword.orElse("")));

        try {
            ensureDatabase(jdbcBaseUrl,
                databaseName,
                dbUsername.orElse(null),
                dbPassword.orElse(null)
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to setup postgres database" ,e);
        }

        return builder.build();
    }

    private void ensureDatabase(String baseUrl, String database, String username, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(baseUrl + "/postgres", username, password)) {

            try (Statement st = connection.createStatement()) {

                try (ResultSet resultSet = st.executeQuery(String.format("SELECT 1 FROM pg_database WHERE datname = '%s'", database))) {

                    if (resultSet.next()) {
                        log.info("Using postgres database: {}", database);
                        return;
                    }

                    st.executeUpdate(String.format("CREATE DATABASE %s", database));
                }

                log.info("Created postgres database: {}", database);
            }
        }
    }
}
