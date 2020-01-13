package com.appdynamics.crazyeventgateway.batchprocessing;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Event;
import com.appdynamics.crazyeventgateway.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class EventWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventWriter.class);

    void write(EventType eventType, List<Event> batchToBeWritten) {
        String path = "src/main/resources/sink/" + eventType.name() + ".log";
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, true);
            for (Event event : batchToBeWritten) {
                outputStream.write(event.toString().getBytes());
            }
        } catch (FileNotFoundException fe) {
            LOGGER.error("File {} does not exist", file);
        } catch (IOException ex) {
            LOGGER.error("Error encountered while writing events to file {}", file);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Error encountered while closing the output stream for sink {}", file);
            }
        }
    }
}
