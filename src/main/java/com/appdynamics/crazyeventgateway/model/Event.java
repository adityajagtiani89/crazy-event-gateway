package com.appdynamics.crazyeventgateway.model;
/*
 * @author Aditya Jagtiani
 */

import javax.validation.constraints.NotEmpty;

public class Event {
    @NotEmpty(message = "Please enter a valid event type (E1, E2, E3, E4")
    private EventType eventType;

    @NotEmpty(message = "Please enter a name for your event")
    private String name;

    public EventType getEventType() {
        return eventType;
    }

    public String getName() {
        return name;
    }

    public Event(EventType eventType, String name) {
        this.eventType = eventType;
        this.name = name;
    }
}
