package com.appdynamics.crazyeventgateway.batchprocessor;

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is responsible for spawning tasks based on incoming event types and initializing their respective timers
 *
 * @author Aditya Jagtiani
 */
public class BatchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchManager.class);
    private static BatchManager batchManager;

    //Four lists, one for each type of event
    private static List<AdTrackingEvent> type1;
    private static List<AdTrackingEvent> type2;
    private static List<AdTrackingEvent> type3;
    private static List<AdTrackingEvent> type4;

    static int maxBatchSize;
    static long batchFlushTimeout;
    static Map<EventType, Boolean> shouldCreateNewEventTimers = new ConcurrentHashMap<>();

    /**
     * BatchManager is a singleton as we do not want each request to have it's own batch manager. In this method, we
     * initialize the BatchManager in thread-safe fashion and some other objects needed for further processing
     *
     * @param maxPermittedBatchSize The maximum permitted batch size
     * @param flushDuration The maximum permitted time duration a batch can reside in the gateway
     *
     * @return An instance of BatchManager
     */
    public static BatchManager getInstance(int maxPermittedBatchSize, long flushDuration) {
        if (batchManager == null) {
            synchronized (BatchManager.class) {
                if (batchManager == null) {
                    batchManager = new BatchManager();
                    type1 = new CopyOnWriteArrayList<>();
                    type2 = new CopyOnWriteArrayList<>();
                    type3 = new CopyOnWriteArrayList<>();
                    type4 = new CopyOnWriteArrayList<>();
                    initTimerMap();
                    maxBatchSize = maxPermittedBatchSize;
                    batchFlushTimeout = flushDuration;
                }
            }
        }
        return batchManager;
    }

    //initializing event timers for each event type
    private static void initTimerMap() {
        shouldCreateNewEventTimers.put(EventType.AD_VIEWED, true);
        shouldCreateNewEventTimers.put(EventType.AD_CLICKED, true);
        shouldCreateNewEventTimers.put(EventType.AD_DISMISSED, true);
        shouldCreateNewEventTimers.put(EventType.AD_CONVERTED_CUSTOMER, true);
    }

    /**
     * This method parses the incoming requests, assigns events based to their respective queues based on event type and
     * starts new tasks per event type
     *
     * @param adTrackingEvents A list of incoming events
     */
    public void processEvents(List<AdTrackingEvent> adTrackingEvents) {
        ExecutorService executorService = Executors.newFixedThreadPool(30);

        Predicate<AdTrackingEvent> type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_VIEWED);
        type1.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));
        LOGGER.info("Found {} events of type {} in the current request. Creating task..", type1.size(),
                EventType.AD_VIEWED.name());

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_CLICKED);
        type2.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));
        LOGGER.info("Found {} events of type {} in the current request. Creating task..", type2.size(),
                EventType.AD_CLICKED.name());

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_DISMISSED);
        type3.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));
        LOGGER.info("Found {} events of type {} in the current request. Creating task..", type3.size(),
                EventType.AD_DISMISSED.name());

        type = adTrackingEvent -> adTrackingEvent.getEventType().equals(EventType.AD_CONVERTED_CUSTOMER);
        type4.addAll(adTrackingEvents.stream().filter(type).collect(Collectors.toList()));
        LOGGER.info("Found {} events of type {} in the current request. Creating task..", type4.size(),
                EventType.AD_CONVERTED_CUSTOMER.name());

        if (type1.size() != 0) {
            executorService.execute(new BatchProcessor(type1, shouldCreateNewEventTimers.get(EventType.AD_VIEWED),
                    EventType.AD_VIEWED));
        }
        if (type2.size() != 0) {
            executorService.execute(new BatchProcessor(type2, shouldCreateNewEventTimers.get(EventType.AD_CLICKED),
                    EventType.AD_CLICKED));
        }
        if (type3.size() != 0) {
            executorService.execute(new BatchProcessor(type3, shouldCreateNewEventTimers.get(EventType.AD_DISMISSED),
                    EventType.AD_DISMISSED));
        }
        if (type4.size() != 0) {
            executorService.execute(new BatchProcessor(type4, shouldCreateNewEventTimers.get(EventType.AD_CONVERTED_CUSTOMER),
                    EventType.AD_CONVERTED_CUSTOMER));
        }
    }

    /**
     * This method flushes any existing pending events in case of a system failure
     */
    public void flushImmediately() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        if (type1.size() != 0) {
            executorService.execute(new BatchProcessor(type1, shouldCreateNewEventTimers.get(EventType.AD_VIEWED),
                    EventType.AD_VIEWED));
        }
        if (type2.size() != 0) {
            executorService.execute(new BatchProcessor(type2, shouldCreateNewEventTimers.get(EventType.AD_CLICKED),
                    EventType.AD_CLICKED));
        }
        if (type3.size() != 0) {
            executorService.execute(new BatchProcessor(type3, shouldCreateNewEventTimers.get(EventType.AD_DISMISSED),
                    EventType.AD_DISMISSED));
        }
        if (type4.size() != 0) {
            executorService.execute(new BatchProcessor(type4, shouldCreateNewEventTimers.get(EventType.AD_CONVERTED_CUSTOMER),
                    EventType.AD_CONVERTED_CUSTOMER));
        }
    }
}