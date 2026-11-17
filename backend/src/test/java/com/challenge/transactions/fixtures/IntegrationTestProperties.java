package com.challenge.transactions.fixtures;

public final class IntegrationTestProperties {

    public static final String ISOLATED_DATASOURCE = "spring.datasource.url=jdbc:h2:mem:transactions-it;DB_CLOSE_DELAY=-1";
    public static final String SCHEMA_CREATION = "spring.jpa.hibernate.ddl-auto=create-drop";
    public static final String SQL_INIT_DISABLED = "spring.sql.init.mode=never";

    private IntegrationTestProperties() {
    }
}
