package com.appdynamics.crazyeventgateway.model;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model class for AdTrackingEvent
 *
 * @author Aditya Jagtiani
 */
public class AdTrackingEvent {
    @NotEmpty(message = "Please enter a valid event type (AD_VIEWED, AD_CLICKED, AD_DISMISSED, AD_CONVERTED_CUSTOMER")
    private EventType eventType;

    @NotEmpty(message = "Please enter a name for your event")
    private String name;

    public EventType getEventType() {
        return eventType;
    }

    public String getName() {
        return name;
    }

    public AdTrackingEvent(EventType eventType, String name) {
        this.eventType = eventType;
        this.name = name;
    }

    @Override
    public String toString() {
        return "["+currentDateTime()+"]" + "\t AdTrackingEvent Type = " + eventType.name() + ", AdTrackingEvent name = " + name + '\n';
    }

    private String currentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(date);
    }
}
