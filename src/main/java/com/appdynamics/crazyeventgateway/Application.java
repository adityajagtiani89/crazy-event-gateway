package com.appdynamics.crazyeventgateway;
/*
 * @author Aditya Jagtiani
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@SpringBootApplication
@Configuration
@PropertySource(value = "classpath:application.properties")
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        //todo: fetch port from config file
        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "9000"));
        LOGGER.info("Starting Crazy Event Gateway Application");
        LOGGER.debug("Starting Crazy Event Gateway Application debug");
        app.run(args);
    }
}
