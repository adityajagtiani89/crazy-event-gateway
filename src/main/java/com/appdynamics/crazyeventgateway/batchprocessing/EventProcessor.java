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
import com.google.common.collect.Lists;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventProcessor implements Runnable {
    private static final int BATCH_SIZE_LIMIT = 10;

    private boolean _flushPending = false;
    private List<Event> events;

    EventProcessor(List<Event> events) {
        this.events = events;
    }

    public void run() {
        processCurrentEventBatch();
    }

    private void processCurrentEventBatch() {
        if (!this._flushPending) {
            this.flushAfterDuration();
        }
        if (events.size() >= BATCH_SIZE_LIMIT) {
            // timer.cancel();
            List<List<Event>> batches = Lists.partition(events, BATCH_SIZE_LIMIT);
            for (List<Event> batch : batches) {
                if (batch.size() == BATCH_SIZE_LIMIT) {
                    try {
                        List<Event> temp = Lists.newArrayList(batch);
                        flushEvents(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    //events.clear();
                    //events.addAll(batch);
                    this.events = new CopyOnWriteArrayList<>(batch);
                    // timer.schedule(timerTask, 3 * 60 * 1000);
                }
            }
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
        for (Event event : events) {
            outputStream.write(event.toString().getBytes());
        }
        outputStream.close();
        events.clear();

    }

//    public static Timer timer;
//    TimerTask timerTask;

    private void flushAfterDuration() {
        //todo: read this value from application.properties file
        this._flushPending = true;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        EventProcessor.this._flushPending = false;
                        try {
                            EventProcessor.this.flushEvents();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                60 * 1000);
    }
}

/*
 * I wanted to clarify one more thing. Let's say we receive 999 requests of type E1 at 1:01:01 and 2 requests of type E1 at 1:01:02.
 * This gets broken into two batches of 1000 and 1.
 *
 * 1. Are both batches immediately sent to the sink as the limit was hit?
 * 2. Is the batch containing only 1000 immediately sent to the sink, and the batch containing 1 retained until 1:01:17 (1:01:02 + 15 seconds),
 *   assuming that no more requests come in until 1:01:17?
 *
 * */
