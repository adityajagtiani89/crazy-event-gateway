package com.appdynamics.crazyeventgateway.batchprocessor;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchProcessor.class, EventWriter.class})
public class BatchProcessorTest {

    @Test
    public void whenBatchSizeIsMetTestOneBatch() throws Exception {
        BatchManager.maxBatchSize = 3;
        EventWriter eventWriter = mock(EventWriter.class);
        PowerMockito.whenNew(EventWriter.class).withAnyArguments().thenReturn(eventWriter);
        BatchProcessor batchProcessor = new BatchProcessor(generateEventListForTesting(), false,
                EventType.AD_CLICKED);
        batchProcessor.run();
        verify(eventWriter, times(1)).write(eq(EventType.AD_CLICKED), Mockito.<AdTrackingEvent>anyList());
    }

    @Test
    public void whenBatchSizeIsMetMultipleBatches() throws Exception {
        BatchManager.maxBatchSize = 2;
        BatchManager.batchFlushTimeout = 3;
        EventWriter eventWriter = mock(EventWriter.class);
        PowerMockito.whenNew(EventWriter.class).withAnyArguments().thenReturn(eventWriter);
        BatchProcessor batchProcessor = new BatchProcessor(generateEventListForTesting(), true,
                EventType.AD_CLICKED);
        batchProcessor.run();
        Thread.sleep(5000);
        verify(eventWriter, times(2)).write(eq(EventType.AD_CLICKED), Mockito.<AdTrackingEvent>anyList());
    }

    private List<AdTrackingEvent> generateEventListForTesting() {
        List<AdTrackingEvent> events = new ArrayList<>();
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event1"));
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event2"));
        events.add(new AdTrackingEvent(EventType.AD_CLICKED, "event3"));
        return events;
    }
}