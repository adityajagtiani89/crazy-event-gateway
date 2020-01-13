
package com.appdynamics.crazyeventgateway.ratelimiting;
/*
 * @author Aditya Jagtiani
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
public class APIRateLimiter {

    private static int maxRequestsPerMinute;
    private static int maxRequestsPerHour;

    private final ConcurrentMap<Long, Integer> hourWindow = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Integer> minuteWindow = new ConcurrentHashMap<>();
    private static APIRateLimiter apiRateLimiter;

    public static APIRateLimiter getInstance(int hourLimit, int minuteLimit) {
        maxRequestsPerHour = hourLimit;
        maxRequestsPerMinute = minuteLimit;
        if (apiRateLimiter == null) {
            apiRateLimiter = new APIRateLimiter();
        }
        return apiRateLimiter;
    }

    private APIRateLimiter() {
        assert maxRequestsPerHour >= maxRequestsPerMinute : "Requests per minute cannot be more than requests per hour";
    }

    public boolean isRequestPermitted() {
        if (!isWithinHourlyLimit()) {
            return false;
        }
        if (!isWithinMinuteLimit()) {
            //maintaining sync between hr and min limits
            long hourWindowStartTime = getWindowStartTime(hourWindow);
            hourWindow.put(hourWindowStartTime, hourWindow.get(hourWindowStartTime) - 1);
            return false;
        }
        return true;
    }

    private boolean isWithinHourlyLimit() {
        long currentRequestTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());

        if(hourWindow.isEmpty()) {
            hourWindow.put(currentRequestTimeInMinutes, 1);
            return true;
        }

        long hourWindowStartTime = getWindowStartTime(hourWindow);
        if(currentRequestTimeInMinutes <= hourWindowStartTime + 60) {
            // request still lies in the same window
            if(hourWindow.get(hourWindowStartTime) + 1 <= maxRequestsPerHour) {
                // within limits, increment counter and return true
                hourWindow.put(hourWindowStartTime, hourWindow.get(hourWindowStartTime) + 1);
                return true;
            }
            else {
                // reject the request as hour limit has been hit
                return false;
            }
        }
        else {
            // request after hour window expires
            hourWindow.clear();
            hourWindow.put(currentRequestTimeInMinutes, 1);
            return true;
        }
    }

    private boolean isWithinMinuteLimit() {
        long currentRequestTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if(minuteWindow.isEmpty()) {
            minuteWindow.put(currentRequestTimeInSeconds, 1);
            return true;
        }

        long minuteWindowStartTime = getWindowStartTime(minuteWindow);
        if(currentRequestTimeInSeconds <= minuteWindowStartTime + 60) {
            // request still lies in the same window
            if(minuteWindow.get(minuteWindowStartTime) + 1 <= maxRequestsPerMinute) {
                // within limits, increment counter and return true
                minuteWindow.put(minuteWindowStartTime, minuteWindow.get(minuteWindowStartTime) + 1);
                return true;
            }
            else {
                // reject the request as minute limit has been hit
                return false;
            }
        }
        else {
            // request after minute window expires
            minuteWindow.clear();
            minuteWindow.put(currentRequestTimeInSeconds, 1);
            return true;
        }
    }

    private long getWindowStartTime(Map<Long, Integer> concurrentWindow) {
        Map.Entry<Long, Integer> entry = concurrentWindow.entrySet().iterator().next();
        return entry.getKey();
    }
}

