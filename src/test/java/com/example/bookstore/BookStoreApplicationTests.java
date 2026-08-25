package com.example.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "aws.access-key=dummy-aws-key",
        "aws.secret-key=dummy-aws-secret",
        "stripe.api-key=dummy-stripe-key",
        "stripe.webhook-secret=dummy-stripe-secret",
        "keycloak.admin.client-secret=dummy-keycloak-secret"
})
class BookStoreApplicationTests {

    @Test
    void contextLoads() {
    }
}