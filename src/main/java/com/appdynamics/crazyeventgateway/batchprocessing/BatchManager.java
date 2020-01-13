package com.appdynamics.crazyeventgateway.batchprocessing;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchManager.class);
    private static BatchManager batchManager;
    private static ArrayList<AdTrackingEvent> type1;
    private static ArrayList<AdTrackingEvent> type2;
    private static ArrayList<AdTrackingEvent> type3;
    private static ArrayList<AdTrackingEvent> type4;
    static int maxBatchSize;
    static long batchFlushTimeout;
    static Map<EventType, Boolean> shouldCreateNewEventTimers = new ConcurrentHashMap<>();

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

    public void flushImmediately() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
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


}