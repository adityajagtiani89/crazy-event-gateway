package com.appdynamics.crazyeventgateway.controller;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.model.Response;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@EnableAutoConfiguration
@RequestMapping("api/v1")
public class EventController {

    @Autowired
    private EventService eventService;

    @RequestMapping("/")
    //todo health check
    public String home() {
        return "Welcome to the crazy event gateway!";
    }

    @RequestMapping(value = "events", method = RequestMethod.POST)
    @ResponseStatus
    public Response create(@Valid @RequestBody Events events) throws Exception {
        return eventService.createEvents(events);
    }

    @RequestMapping(value = "events", method = RequestMethod.GET)
    public Events getEvents() {
        return eventService.getEvents();
    }
}