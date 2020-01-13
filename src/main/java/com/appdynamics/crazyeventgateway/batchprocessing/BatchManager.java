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
import com.google.common.collect.Lists;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchManager {

    //todo: figure out how to make this scalable if we add new event types
    private static BatchManager batchManager;
    private static ArrayList<Event> type1;
    private static ArrayList<Event> type2;
    private static ArrayList<Event> type3;
    private static ArrayList<Event> type4;
    private boolean shouldStartEventTimer = true;

    public static BatchManager getInstance() {
        if (batchManager == null) {
            synchronized (BatchManager.class) {
                if (batchManager == null) {
                    batchManager = new BatchManager();
                    type1 = new ArrayList<>();
                    type2 = new ArrayList<>();
                    type3 = new ArrayList<>();
                    type4 = new ArrayList<>();
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
            executorService.execute(new BatchProcessor(type1));
        }
        if (type2.size() != 0) {
            executorService.execute(new BatchProcessor(type2));
        }
        if (type3.size() != 0) {
            executorService.execute(new BatchProcessor(type3));
        }
        if (type4.size() != 0) {
            executorService.execute(new BatchProcessor(type4));
        }
    }

    public class BatchProcessor implements Runnable {
        private static final int BATCH_SIZE_LIMIT = 10;

        List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }

        private List<Event> events;

        BatchProcessor(List<Event> events) {
            this.events = events;
        }

        public void run() {
            processCurrentEventBatch();
        }

        private void processCurrentEventBatch() {
            List<Event> remainingEvents = new ArrayList<>();
            List<Event> tempList;
            if (BatchManager.getInstance().shouldStartEventTimer) {
                this.flushAfterDuration();
            }
            if (events.size() >= BATCH_SIZE_LIMIT) {
                tempList = new ArrayList<>();
                tempList.addAll(events);
                List<List<Event>> batches = Lists.partition(tempList, BATCH_SIZE_LIMIT);
                for (List<Event> batch : batches) {
                    if (batch.size() == BATCH_SIZE_LIMIT) {
                        try {
                            List<Event> temp = Lists.newArrayList(batch);
                            flushEvents(temp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                         remainingEvents.addAll(batch);
                        // timer.schedule(timerTask, 3 * 60 * 1000);
                    }
                }
                this.events.clear();
                this.events.addAll(remainingEvents);
/*                for (Event event : remainingEvents) {
                    this.events.add(event);
                }*/
            }
        }

        private void flushEvents(List<Event> batch) throws IOException {
            OutputStream outputStream = new FileOutputStream(("src/main/resources/event1.log"), true);
            for (Event event : batch) {
                outputStream.write(event.toString().getBytes());
            }
            outputStream.close();
            batch.clear();
        }


        private void flushEvents() throws IOException {
            OutputStream outputStream = new FileOutputStream(("src/main/resources/event1.log"), true);
            for (Event event : getEvents()) {
                outputStream.write(event.toString().getBytes());
            }
            outputStream.close();
            events.clear();
        }

//    public static Timer timer;
//    TimerTask timerTask;

        private void flushAfterDuration() {
            //todo: read this value from application.properties file
            BatchManager.getInstance().shouldStartEventTimer = false;

            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                System.out.println("Executing Task At " + System.nanoTime());
                try {
                    this.flushEvents();
                    BatchManager.getInstance().shouldStartEventTimer = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            System.out.println("Submitting task at " + System.nanoTime() + " to be executed after 5 seconds.");
            scheduledExecutorService.schedule(task, 20, TimeUnit.SECONDS);
        }
    }

}