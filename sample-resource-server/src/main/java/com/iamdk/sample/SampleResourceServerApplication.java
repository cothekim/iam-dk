package com.iamdk.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IAM-DK Sample Resource Server
 * Demonstrates OIDC authentication with IAM-DK
 */
@SpringBootApplication
public class SampleResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleResourceServerApplication.class, args);
    }
}
