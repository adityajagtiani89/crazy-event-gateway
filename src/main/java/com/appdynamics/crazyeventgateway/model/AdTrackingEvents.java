package com.appdynamics.crazyeventgateway.model;

import java.util.List;

/**
 * Model class for AdTrackingEvents
 *
 * @author Aditya Jagtiani
 */
public class AdTrackingEvents {

    public List<AdTrackingEvent> getAdTrackingEvents() {
        return adTrackingEvents;
    }

    public void setAdTrackingEvents(List<AdTrackingEvent> adTrackingEvents) {
        this.adTrackingEvents = adTrackingEvents;
    }

    private List<AdTrackingEvent> adTrackingEvents;

 }
