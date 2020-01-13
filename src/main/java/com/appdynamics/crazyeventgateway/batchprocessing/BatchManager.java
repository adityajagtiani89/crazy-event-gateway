package com.appdynamics.crazyeventgateway.batchprocessing;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchManager {

    private static BatchManager batchManager;
    private static ArrayList<AdTrackingEvent> type1;
    private static ArrayList<AdTrackingEvent> type2;
    private static ArrayList<AdTrackingEvent> type3;
    private static ArrayList<AdTrackingEvent> type4;
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
        // todo = handle leftover events in case of an exception or service crash
        shouldCreateNewEventTimers.put(EventType.AD_VIEWED, true);
        shouldCreateNewEventTimers.put(EventType.AD_CLICKED, true);
        shouldCreateNewEventTimers.put(EventType.AD_DISMISSED, true);
        shouldCreateNewEventTimers.put(EventType.AD_CONVERTED_CUSTOMER, true);
    }

    // AdTrackingEvents ingested will be put on the respective kind of adTrackingEvents list
    public void processEvents(List<AdTrackingEvent> adTrackingEvents) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        Predicate<AdTrackingEvent> type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_VIEWED);
        type1.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_CLICKED);
        type2.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_DISMISSED);
        type3.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_CONVERTED_CUSTOMER);
        type4.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));

        if (type1.size() != 0) {
            executorService.execute(new BatchProcessor(type1, shouldCreateNewEventTimers.get(EventType.AD_VIEWED),EventType.AD_VIEWED));
        }
        if (type2.size() != 0) {
            executorService.execute(new BatchProcessor(type2, shouldCreateNewEventTimers.get(EventType.AD_CLICKED), EventType.AD_CLICKED));
        }
        if (type3.size() != 0) {
            executorService.execute(new BatchProcessor(type3, shouldCreateNewEventTimers.get(EventType.AD_DISMISSED), EventType.AD_DISMISSED));
        }
        if (type4.size() != 0) {
            executorService.execute(new BatchProcessor(type4, shouldCreateNewEventTimers.get(EventType.AD_CONVERTED_CUSTOMER), EventType.AD_CONVERTED_CUSTOMER));
        }
    }

    public class BatchProcessor implements Runnable {
        private List<AdTrackingEvent> adTrackingEvents;
        private boolean shouldStartEventTimer;
        private EventType eventType;

        BatchProcessor(List<AdTrackingEvent> adTrackingEvents, boolean shouldStartEventTimer, EventType eventType) {
            this.adTrackingEvents = adTrackingEvents;
            this.shouldStartEventTimer = shouldStartEventTimer;
            this.eventType = eventType;
        }

        public void run() {
            processCurrentEventBatch(shouldStartEventTimer);
        }

        private void processCurrentEventBatch(boolean shouldStartEventTimer) {
            List<AdTrackingEvent> remainingAdTrackingEvents = new ArrayList<>();
            List<AdTrackingEvent> tempList;
            if (shouldStartEventTimer) {
                this.flushAfterDuration(this.eventType);
            }
            if (adTrackingEvents.size() >= maxBatchSize) {
                tempList = new ArrayList<>();
                tempList.addAll(adTrackingEvents);
                List<List<AdTrackingEvent>> batches = Lists.partition(tempList, maxBatchSize);
                flushCompletedBatches(remainingAdTrackingEvents, batches);
                this.adTrackingEvents.clear();
                this.adTrackingEvents.addAll(remainingAdTrackingEvents);
            }
        }

        private void flushCompletedBatches(List<AdTrackingEvent> remainingAdTrackingEvents, List<List<AdTrackingEvent>> batches) {
            for (List<AdTrackingEvent> batch : batches) {
                if (batch.size() == maxBatchSize) {
                    try {
                        List<AdTrackingEvent> temp = Lists.newArrayList(batch);
                        flushEvents(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    // add overflow batch adTrackingEvents to remainingAdTrackingEvents
                    remainingAdTrackingEvents.addAll(batch);
                }
            }
        }

        private void flushEvents(List<AdTrackingEvent> currentBatch) throws IOException {
            new EventWriter().write(eventType, currentBatch);
            currentBatch.clear();
        }

        private void flushAfterDuration(EventType eventType) {
            shouldCreateNewEventTimers.put(eventType, false);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                System.out.println("Executing Task At " + System.nanoTime());
                try {
                    this.flushEvents(this.adTrackingEvents);
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