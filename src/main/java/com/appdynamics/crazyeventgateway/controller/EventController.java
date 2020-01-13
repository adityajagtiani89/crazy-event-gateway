package com.appdynamics.crazyeventgateway.controller;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.ratelimiting.APIRateLimiter;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@EnableAutoConfiguration
@RequestMapping("api/v1")
public class EventController {
    @Autowired
    private EventService eventService;

    @Value("${app.rate.limiter.maxRequestsPerHour}")
    private int hourlyLimit;

    @Value("${app.rate.limiter.maxRequestsPerMinute}")
    private int minLimit;

    @Autowired
    private APIRateLimiter apiRateLimiter;

    @RequestMapping("/")
    public String home() {
        return "Welcome to the crazy event gateway!";
    }

    @PostMapping(path = "/events", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> ingestEvents(@Valid @RequestBody Events events) {
        if(events == null) {
            return ResponseEntity.noContent().build();
        }
        apiRateLimiter = APIRateLimiter.getInstance(hourlyLimit, minLimit);
        if (!apiRateLimiter.allowRequest()) {
            return ResponseEntity.noContent().build();
        }
        try {
            eventService.createEvents(events);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .buildAndExpand(events)
                    .toUri();
            return ResponseEntity.created(location).build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}