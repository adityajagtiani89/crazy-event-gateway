package com.appdynamics.crazyeventgateway;
/*
 * @author Aditya Jagtiani
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@SpringBootApplication
@Configuration
@PropertySource(value = "classpath:application.properties")
public class Application {

    public static void main(String[] args) {
        //todo: fetch port from config file
        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "9000"));
        app.run(args);
    }

    //todo: figure out request response
    // todo: multiple event types testing
    // todo: event writer
    // todo : logging
    // todo: app.properties
    // todo: unit tests
    // todo: javadocs
    // todo: write a sensible response to sink
    // todo: sleep
}
