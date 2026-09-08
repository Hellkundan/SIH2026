package com.dixy.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DIXY Backend — Application Entry Point
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   @Configuration     — this class can define Spring beans
 *   @EnableAutoConfiguration — let Spring Boot guess what to configure
 *   @ComponentScan     — scan this package and sub-packages for Spring components
 *
 * When you run this class, Spring Boot:
 *   1. Starts an embedded Tomcat web server (port 8080 by default)
 *   2. Connects to PostgreSQL
 *   3. Creates database tables (Hibernate DDL auto)
 *   4. Sets up Spring Security with our configuration
 *   5. Registers all @RestController endpoints
 */
@SpringBootApplication
public class DixyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DixyBackendApplication.class, args);
    }
}
