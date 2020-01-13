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


import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.appdynamics.crazyeventgateway.batchprocessing.BatchManager.*;

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

        scheduledExecutorService.schedule(task, batchFlushTimeout, TimeUnit.SECONDS);
    }
}
