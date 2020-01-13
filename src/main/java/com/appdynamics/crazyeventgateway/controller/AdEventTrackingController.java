package com.appdynamics.crazyeventgateway.controller;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.AdTrackingEvents;
import com.appdynamics.crazyeventgateway.ratelimiting.APIRateLimiter;
import com.appdynamics.crazyeventgateway.service.AdEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@EnableAutoConfiguration
@RequestMapping("api/v1/ads")
public class AdEventTrackingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdEventTrackingController.class);

    @Autowired
    private AdEventService adEventService;

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
    public ResponseEntity<String> ingestEvents(@Valid @RequestBody AdTrackingEvents adTrackingEvents) {
        if(adTrackingEvents == null || adTrackingEvents.getAdTrackingEvents() == null) {
            LOGGER.error("Request failed, no events present in request body");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request failed, no events present in request body", null);
        }
        apiRateLimiter = APIRateLimiter.getInstance(hourlyLimit, minLimit);
        if (!apiRateLimiter.isRequestPermitted()) {
            LOGGER.error("Request failed as API rate limits were hit. Please try again later");
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Request failed as API rate limits were hit. Please try again later", null);
        }
        try {
            adEventService.createEvents(adTrackingEvents);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .buildAndExpand(adTrackingEvents)
                    .toUri();
            LOGGER.info("Request processed successfully");
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>("SUCCESS", responseHeaders, HttpStatus.ACCEPTED);
        }
        catch (Exception e) {
            LOGGER.error("Bad request, check request body");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad request, check request body", null);
        }
    }
}