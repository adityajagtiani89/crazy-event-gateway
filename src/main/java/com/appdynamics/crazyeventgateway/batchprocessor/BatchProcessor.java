package com.appdynamics.crazyeventgateway.batchprocessor;

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.appdynamics.crazyeventgateway.batchprocessor.BatchManager.*;

/**
 * This class ingests incoming events based on the specified batch and time limits
 *
 * @author Aditya Jagtiani
 */
public class BatchProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);
    private List<AdTrackingEvent> adTrackingEvents;
    private boolean shouldStartEventTimer;
    private EventType eventType;

    BatchProcessor(List<AdTrackingEvent> adTrackingEvents, boolean shouldStartEventTimer, EventType eventType) {
        this.adTrackingEvents = adTrackingEvents;
        this.shouldStartEventTimer = shouldStartEventTimer;
        this.eventType = eventType;
    }

    public void run() {
        LOGGER.debug("Attempting to ingest current batch with {} events", adTrackingEvents.size());
        processCurrentEventBatch();
    }

    private void processCurrentEventBatch() {
        List<AdTrackingEvent> remainingAdTrackingEvents = new ArrayList<>();
        List<AdTrackingEvent> currentEventsToBeProcessed;
        if (shouldStartEventTimer) {
            this.flushEventsOnTimeout(this.eventType); // this spawns a new thread that starts a 15 second timer on the current batch
        }
        if (adTrackingEvents.size() >= maxBatchSize) { // checking if the incoming request exceeds the maximum permitted batch size
            currentEventsToBeProcessed = new ArrayList<>(adTrackingEvents);
            LOGGER.debug("Batch size limit hit. Partitioning batch");
            List<List<AdTrackingEvent>> batches = Lists.partition(currentEventsToBeProcessed, maxBatchSize);
            flushBatchesWhenLimitIsHit(remainingAdTrackingEvents, batches); //flush batch immediately when the max permitted batch size limit is hit
            this.adTrackingEvents.clear();
            this.adTrackingEvents.addAll(remainingAdTrackingEvents); //storing only the leftover events from the current request
        }
    }

    private void flushBatchesWhenLimitIsHit(List<AdTrackingEvent> remainingAdTrackingEvents, List<List<AdTrackingEvent>> batches) {
        for (List<AdTrackingEvent> batch : batches) {
            if (batch.size() == maxBatchSize) {
                LOGGER.debug("Max batch size limit hit for current batch. Flushing");
                List<AdTrackingEvent> completedBatchToBeFlushed = Lists.newArrayList(batch);
                flushEvents(completedBatchToBeFlushed);
            } else {
                LOGGER.debug("Waiting for 15 seconds to flush leftover events from request");
                remainingAdTrackingEvents.addAll(batch);
            }
        }
    }

    private void flushEvents(List<AdTrackingEvent> currentBatch) {
        new EventWriter().write(eventType, currentBatch);
        currentBatch.clear();
    }

    private void flushEventsOnTimeout(EventType eventType) {
        shouldCreateNewEventTimers.put(eventType, false);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            LOGGER.debug("Executing Task At {}", System.nanoTime());
            this.flushEvents(this.adTrackingEvents); //flush batch every 15 seconds
            shouldCreateNewEventTimers.put(eventType, true);

        };
        scheduledExecutorService.schedule(task, batchFlushTimeout, TimeUnit.SECONDS);
    }
}