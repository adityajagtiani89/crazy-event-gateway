package com.appdynamics.crazyeventgateway.controller;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.ratelimiting.APIRateLimiter;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@EnableAutoConfiguration
@RequestMapping("api/v1")
public class EventController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);
    @Autowired
    private EventService eventService;

    @Value("${ratelimiter.maxRequestsPerHour}")
    private int hourlyLimit;

    @Value("${ratelimiter.maxRequestsPerMinute}")
    private int minLimit;

    @Autowired
    private APIRateLimiter apiRateLimiter;

    @RequestMapping("/")
    //todo health check
    public String home() {
        return "Welcome to the crazy event gateway!";
    }

    @RequestMapping(value = "events", method = RequestMethod.POST)
    @ResponseStatus
    public void create(@Valid @RequestBody Events events) throws Exception {
        LOGGER.info("Attempting to ingest {} events", events.getEvents().size());
        apiRateLimiter = APIRateLimiter.getInstance();
        if (apiRateLimiter.allowRequest()) {
            eventService.createEvents(events);
        }
        //todo: Create error message with rate rejection and return
        //todo: Add logger to project
    }
}