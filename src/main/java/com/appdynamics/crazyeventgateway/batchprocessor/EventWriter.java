package com.appdynamics.crazyeventgateway.batchprocessor;

import com.appdynamics.crazyeventgateway.model.AdTrackingEvent;
import com.appdynamics.crazyeventgateway.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * This class writes processed events to their respective sinks
 *
 * @author Aditya Jagtiani
 */
class EventWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventWriter.class);

    /**
     * This method writes a batch of events to the respective sink
     *
     * @param eventType The type of event
     * @param batchToBeWritten The batch of events to be written
     */
    void write(EventType eventType, List<AdTrackingEvent> batchToBeWritten) {
        String path = "src/main/resources/sink/" + eventType.name() + ".log";
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, true);
            for (AdTrackingEvent adTrackingEvent : batchToBeWritten) {
                outputStream.write(adTrackingEvent.toString().getBytes());
            }
            LOGGER.info("Successfully published {} events to sink {}", batchToBeWritten.size(), file.getName());
        } catch (FileNotFoundException fe) {
            LOGGER.error("File {} does not exist", file, fe);
        } catch (IOException ex) {
            LOGGER.error("Error encountered while writing events to file {}", file, ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Error encountered while closing the output stream for sink {}", file, ex);
            }
        }
    }
}
