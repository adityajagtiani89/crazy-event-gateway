/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 *//*


package com.appdynamics.crazyeventgateway.batchprocessor;
*/
/*
 * @author Aditya Jagtiani
 *//*



import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchProcessor.class, EventWriter.class})
public class BatchProcessorTest {

    @Before
    public void setup() {
        BatchManager.maxBatchSize = 3;
    }

    @Test
    public void whenBatchSizeIsMetTest() throws Exception {
        EventWriter eventWriter = mock(EventWriter.class);
        PowerMockito.whenNew(EventWriter.class).withAnyArguments().thenReturn(eventWriter);
        BatchProcessor batchProcessor = new BatchProcessor(generateEventListForTesting(), false,
                EventType.AD_CLICKED);
        batchProcessor.run();
        //verify(eventWriter, times(1)).write(EventType.AD_CLICKED, Mockito.any(List.class));
    }

    private List<AdTrackingEvent> generateEventListForTesting() {
        List<AdTrackingEvent> events = new ArrayList<>();
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event1"));
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event2"));
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event3"));
        return events;
    }
}
*/
