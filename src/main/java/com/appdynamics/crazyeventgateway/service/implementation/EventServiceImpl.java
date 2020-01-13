package com.appdynamics.crazyeventgateway.service.implementation;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.batchprocessing.BatchManager;
import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    @Value("${app.batch.processor.max.event.size}")
    private int maxEventListSize;

    @Value("${app.batch.processor.flush.duration}")
    private long flushDuration;

    @Override
    public void createEvents(Events events) {
        BatchManager.getInstance(maxEventListSize, flushDuration).processEvents(events.getEvents());
    }
}