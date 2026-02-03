package com.iamdk.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * IAM-DK Authorization Server
 * OAuth2/OIDC Provider using Spring Authorization Server
 */
@SpringBootApplication
@EnableJpaAuditing
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
