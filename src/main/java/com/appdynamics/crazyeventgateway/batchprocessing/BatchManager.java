/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.batchprocessing;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Event;
import com.appdynamics.crazyeventgateway.model.EventType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchManager {

    //todo: figure out how to make this scalable if we add new event types
    private static BatchManager batchManager;
    private static CopyOnWriteArrayList<Event> type1;
    private static CopyOnWriteArrayList<Event> type2;
    private static CopyOnWriteArrayList<Event> type3;
    private static CopyOnWriteArrayList<Event> type4;

    public static BatchManager getInstance() {
        if (batchManager == null) {
            synchronized (BatchManager.class) {
                if (batchManager == null) {
                    batchManager = new BatchManager();
                    type1 = new CopyOnWriteArrayList<>();
                    type2 = new CopyOnWriteArrayList<>();
                    type3 = new CopyOnWriteArrayList<>();
                    type4 = new CopyOnWriteArrayList<>();
                }
            }
        }
        return batchManager;
    }

    // Events ingested will be put on specific kind of events list
    public void processEvents(List<Event> events) {
        //todo take this frm properties file
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        Predicate<Event> type = event -> event.getEventType().equals(EventType.E1);
        type1.addAll(events.stream().filter(type).collect(Collectors.toList()));

        type = event -> event.getEventType().equals(EventType.E2);
        type2.addAll(events.stream().filter(type).collect(Collectors.toList()));

        type = event -> event.getEventType().equals(EventType.E3);
        type3.addAll(events.stream().filter(type).collect(Collectors.toList()));

        type = event -> event.getEventType().equals(EventType.E4);
        type4.addAll(events.stream().filter(type).collect(Collectors.toList()));

        if (type1.size() != 0) {
            executorService.execute(new EventProcessor(type1));
            if (type1.size() > 15) {
                type1.clear();
            }
        }
        if (type2.size() != 0) {
            executorService.execute(new EventProcessor(type2));
            type2.clear();
        }
        if (type3.size() != 0) {
            executorService.execute(new EventProcessor(type3));
            type3.clear();
        }
        if (type4.size() != 0) {
            executorService.execute(new EventProcessor(type4));
            type4.clear();
        }
    }
}