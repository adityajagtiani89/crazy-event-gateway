package com.appdynamics.crazyeventgateway.service.implementation;
/*
 * @author Aditya Jagtiani
 */


import com.appdynamics.crazyeventgateway.model.Event;
import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.model.Response;
import com.appdynamics.crazyeventgateway.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class EventServiceImpl implements EventService {
    ObjectMapper mapper = new ObjectMapper();


    @Override
    public void createEvents(Events events) throws Exception {
        for (Event event : events.getEvents()) {
            // Save JSON string to file
            FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/events.json", true);
            mapper.writeValue(fileOutputStream, event);
            fileOutputStream.close();
        }

        //TODO: return all events from file

    }

}
