/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.batchprocessor;
/*
 * @author Aditya Jagtiani
 */


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
        List<AdTrackingEvent> tempList;
        if (shouldStartEventTimer) {
            this.flushAfterDuration(this.eventType);
        }
        if (adTrackingEvents.size() >= maxBatchSize) {
            tempList = new ArrayList<>();
            tempList.addAll(adTrackingEvents);
            LOGGER.debug("Partitioning batch");
            List<List<AdTrackingEvent>> batches = Lists.partition(tempList, maxBatchSize);
            flushCompletedBatches(remainingAdTrackingEvents, batches);
            this.adTrackingEvents.clear();
            this.adTrackingEvents.addAll(remainingAdTrackingEvents);
        }
    }

    private void flushCompletedBatches(List<AdTrackingEvent> remainingAdTrackingEvents, List<List<AdTrackingEvent>> batches) {
        for (List<AdTrackingEvent> batch : batches) {
            if (batch.size() == maxBatchSize) {
                LOGGER.debug("Max batch size limit hit for current batch. Flushing");
                List<AdTrackingEvent> temp = Lists.newArrayList(batch);
                flushEvents(temp);
            } else {
                LOGGER.debug("Waiting for 15 seconds to flush leftover events from request");
                // add overflow batch adTrackingEvents to remainingAdTrackingEvents
                remainingAdTrackingEvents.addAll(batch);
            }
        }
    }

    private void flushEvents(List<AdTrackingEvent> currentBatch) {
        new EventWriter().write(eventType, currentBatch);
        currentBatch.clear();
    }

    private void flushAfterDuration(EventType eventType) {
        shouldCreateNewEventTimers.put(eventType, false);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            LOGGER.debug("Executing Task At {}", System.nanoTime());
            this.flushEvents(this.adTrackingEvents);
            shouldCreateNewEventTimers.put(eventType, true);

        };
        scheduledExecutorService.schedule(task, batchFlushTimeout, TimeUnit.SECONDS);
    }
}
