/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.service;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.crazyeventgateway.model.Events;
import com.appdynamics.crazyeventgateway.model.Response;

public interface EventService {

    Response createEvents(Events events) throws Exception;

    Events getEvents();
}
