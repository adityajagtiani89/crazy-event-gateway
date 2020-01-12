/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.util;
/*
 * @author Aditya Jagtiani
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.Map;

public class CrazyEventGatewayUtils {

    // todo - this sucks, pls change it
    public static int readValueFromConfig(String configName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File file = new File(classLoader.getResource("config.yml").getFile());
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            Map configs = om.readValue(file, Map.class);
            return Integer.parseInt(configs.get(configName).toString());
        }
        catch (Exception ex) {
            System.err.println("Error encountered while reading config.yml");
        }
        return -1;
    }
}
