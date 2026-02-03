package com.iamdk.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * IAM-DK Directory Service
 * User/Group Management, SCIM API, Provisioning
 */
@SpringBootApplication
@EnableJpaAuditing
public class DirectoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DirectoryServiceApplication.class, args);
    }
}
