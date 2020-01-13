package com.appdynamics.crazyeventgateway.service.implementation;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.batchprocessing.BatchManager;
import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    @Override
    public void createEvents(Events events) {

        BatchManager.getInstance().processEvents(events.getEvents());
        //TODO: return all events from file
    }
}