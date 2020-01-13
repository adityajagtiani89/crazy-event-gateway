/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.crazyeventgateway.ratelimiting;
/*
 * @author Aditya Jagtiani
 */


import org.junit.Assert;
import org.junit.Test;

public class APIRateLimiterTest {

    @Test
    public void whenMinuteLimitsAndHourLimitsArePassedFirstRequestTest() {
        APIRateLimiter apiRateLimiter = APIRateLimiter.getInstance(10, 1);
        Assert.assertTrue(apiRateLimiter.isRequestPermitted());
    }

    @Test
    public void whenMinuteLimitsFailAndHourLimitsPassTest() {
        APIRateLimiter apiRateLimiter = APIRateLimiter.getInstance(100, 10);
        for(int i = 0; i < 10; i ++) {
            Assert.assertTrue(apiRateLimiter.isRequestPermitted());
        }
        Assert.assertFalse(apiRateLimiter.isRequestPermitted());
    }

    @Test
    public void whenMinuteLimitsPassAndHourlyLimitsFailTest() throws Exception {
        APIRateLimiter apiRateLimiter = APIRateLimiter.getInstance(1, 1);
        Assert.assertTrue(apiRateLimiter.isRequestPermitted());
        Thread.sleep(60000);
        Assert.assertFalse(apiRateLimiter.isRequestPermitted());
    }
}
