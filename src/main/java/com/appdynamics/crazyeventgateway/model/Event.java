package com.appdynamics.crazyeventgateway.model;
/*
 * @author Aditya Jagtiani
 */

import javax.validation.constraints.NotEmpty;

public class Event {

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty(message = "Please enter a valid event type (E1, E2, E3, E4")
    private EventType eventType;

    @NotEmpty(message = "Please enter a name for your event")
    private String name;
}
