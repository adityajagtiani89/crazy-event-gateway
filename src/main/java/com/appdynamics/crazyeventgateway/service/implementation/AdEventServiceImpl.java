package com.appdynamics.crazyeventgateway.service.implementation;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.batchprocessing.BatchManager;
import com.appdynamics.crazyeventgateway.model.AdTrackingEvents;
import com.appdynamics.crazyeventgateway.service.AdEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdEventServiceImpl implements AdEventService {

    @Value("${app.batch.processor.max.event.size}")
    private int maxEventListSize;

    @Value("${app.batch.processor.flush.duration}")
    private long flushDuration;

    @Override
    public void createEvents(AdTrackingEvents adTrackingEvents) {
        BatchManager.getInstance(maxEventListSize, flushDuration).processEvents(adTrackingEvents.getAdTrackingEvents());
    }
}