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
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchManager {

    private static BatchManager batchManager;
    private static ArrayList<Event> type1;
    private static ArrayList<Event> type2;
    private static ArrayList<Event> type3;
    private static ArrayList<Event> type4;
    private static int maxBatchSize;
    private static long batchFlushTimeout;
    private static Map<EventType, Boolean> shouldCreateNewEventTimers = new ConcurrentHashMap<>();

    public static BatchManager getInstance(int maxEventListSize, long flushDuration) {
        if (batchManager == null) {
            synchronized (BatchManager.class) {
                if (batchManager == null) {
                    batchManager = new BatchManager();
                    type1 = new ArrayList<>();
                    type2 = new ArrayList<>();
                    type3 = new ArrayList<>();
                    type4 = new ArrayList<>();
                    initTimerMap();
                    maxBatchSize = maxEventListSize;
                    batchFlushTimeout = flushDuration;
                }
            }
        }
        return batchManager;
    }

    private static void initTimerMap() {
        // todo: try a better way to init this map
        shouldCreateNewEventTimers.put(EventType.E1, true);
        shouldCreateNewEventTimers.put(EventType.E2, true);
        shouldCreateNewEventTimers.put(EventType.E3, true);
        shouldCreateNewEventTimers.put(EventType.E4, true);
    }

    private static void resetEventTypeShouldStartTimer(EventType eventType) {

    }

    // Events ingested will be put on the respective kind of events list
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
            executorService.execute(new BatchProcessor(type1, shouldCreateNewEventTimers.get(EventType.E1),EventType.E1));
        }
        if (type2.size() != 0) {
            executorService.execute(new BatchProcessor(type2, shouldCreateNewEventTimers.get(EventType.E2), EventType.E2));
        }
        if (type3.size() != 0) {
            executorService.execute(new BatchProcessor(type3, shouldCreateNewEventTimers.get(EventType.E3), EventType.E3));
        }
        if (type4.size() != 0) {
            executorService.execute(new BatchProcessor(type4, shouldCreateNewEventTimers.get(EventType.E4), EventType.E4));
        }
    }

    public class BatchProcessor implements Runnable {
        private List<Event> events;
        private boolean shouldStartEventTimer;
        private EventType eventType;

        BatchProcessor(List<Event> events, boolean shouldStartEventTimer, EventType eventType) {
            this.events = events;
            this.shouldStartEventTimer = shouldStartEventTimer;
            this.eventType = eventType;
        }

        public void run() {
            processCurrentEventBatch(shouldStartEventTimer);
        }

        private void processCurrentEventBatch(boolean shouldStartEventTimer) {
            List<Event> remainingEvents = new ArrayList<>();
            List<Event> tempList;
            if (shouldStartEventTimer) {
                this.flushAfterDuration(this.eventType);
            }
            if (events.size() >= maxBatchSize) {
                tempList = new ArrayList<>();
                tempList.addAll(events);
                List<List<Event>> batches = Lists.partition(tempList, maxBatchSize);
                flushCompletedBatches(remainingEvents, batches);
                this.events.clear();
                this.events.addAll(remainingEvents);
            }
        }

        private void flushCompletedBatches(List<Event> remainingEvents, List<List<Event>> batches) {
            for (List<Event> batch : batches) {
                if (batch.size() == maxBatchSize) {
                    try {
                        List<Event> temp = Lists.newArrayList(batch);
                        flushEvents(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    // add overflow batch events to remainingEvents
                    remainingEvents.addAll(batch);
                }
            }
        }

        private void flushEvents(List<Event> currentBatch) throws IOException {
            new EventWriter().write(eventType, currentBatch);
            currentBatch.clear();
        }

        private void flushAfterDuration(EventType eventType) {
            //todo: read this value from application.properties file
            shouldCreateNewEventTimers.put(eventType, false);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                System.out.println("Executing Task At " + System.nanoTime());
                try {
                    this.flushEvents(this.events);
                    shouldCreateNewEventTimers.put(eventType, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            System.out.println("Submitting task at " + System.nanoTime() + " to be executed after 30 seconds.");
            scheduledExecutorService.schedule(task, batchFlushTimeout, TimeUnit.SECONDS);
        }
    }
}