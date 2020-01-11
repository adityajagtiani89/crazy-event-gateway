package com.appdynamics.crazyeventgateway.service.implementation;
/*
 * @author Aditya Jagtiani
 */


import com.appdynamics.crazyeventgateway.model.Event;
import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.repository.EventRepository;
import com.appdynamics.crazyeventgateway.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventServiceImpl implements EventService {
    @Autowired
    private EventRepository eventRepository;

    @Override
    public Events createEvents(Events events) {
        return eventRepository.createEvents(events);
    }

    @Override
    public Events getEvents() {
        return eventRepository.getEvents();
    }

}
