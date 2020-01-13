/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.model;
/*
 * @author Aditya Jagtiani
 */


import java.util.List;

public class AdTrackingEvents {

    public List<AdTrackingEvent> getAdTrackingEvents() {
        return adTrackingEvents;
    }

    public void setAdTrackingEvents(List<AdTrackingEvent> adTrackingEvents) {
        this.adTrackingEvents = adTrackingEvents;
    }

    private List<AdTrackingEvent> adTrackingEvents;

 }
