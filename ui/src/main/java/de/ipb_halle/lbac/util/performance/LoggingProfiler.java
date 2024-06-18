/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.util.performance;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ExternalContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author swittche
 */
@Singleton
@Startup
public class LoggingProfiler {

    private class ProfileRecord {
        private long threadId;
        private long timestamp;

        public ProfileRecord() {
            threadId = Thread.currentThread().getId();
            timestamp = new Date().getTime();
        }
        
        public long getTime() {
            return timestamp;
        }
    }
    
    private Logger logger;
    private Map<String, Map<String, ProfileRecord>> beanStartTimes;
    private Map<String, Map<String, ProfileRecord>> beanEndTimes;

    public LoggingProfiler() {
        logger = LogManager.getLogger(LoggingProfiler.class.getName());
        beanStartTimes = new HashMap<> ();
        beanEndTimes = new HashMap<> ();
    }
    
    private String getSessionId() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            return context.getExternalContext().getSessionId(false);
        } catch (Exception e) {
            return "noSession";
        }
    }
    
    //1.Method init
    public void profilerStart(String beanName) {
//        
//        String sessionId = getSessionId();
//        Map<String, ProfileRecord> startMap = beanStartTimes
//                .getOrDefault(sessionId, new HashMap<> ());
//        ProfileRecord record = startMap
//                .getOrDefault(beanName, new ProfileRecord());
//        
//        startMap.put(beanName, record);
//        beanStartTimes.put(sessionId, startMap);
    }

    //2.Method profilerAddTimer
    public void profilerStop(String beanName) {
//        String sessionId = getSessionId();
//        Map<String, ProfileRecord> endMap = beanEndTimes
//                .getOrDefault(sessionId, new HashMap<> ());
//        endMap.put(beanName, new ProfileRecord());
//        beanEndTimes.put(sessionId, endMap);
    }

    //3.Method showMap
    public void showMap() {
        long now = new Date().getTime();
        for (String sessionId : beanStartTimes.keySet()) {
            long endTime = 0;
            logger.info("************ SESSION: {} ************", sessionId);
            Map<String, ProfileRecord> startMap = beanStartTimes.get(sessionId);
            Map<String, ProfileRecord> endMap = beanEndTimes.get(sessionId);
            for (String beanName : startMap.keySet()) {
                ProfileRecord start = startMap.get(beanName);
                ProfileRecord end = null;
                if (endMap != null) {
                    end = endMap.get(beanName);
                } else {
                    logger.warn("Missing profiling endpoint for bean {}", beanName);
                }
                if (end != null) {
                    logger.info(String.format(
                            "%52s %d",
                            beanName,
                            end.getTime() - start.getTime()));
                    endTime = (end.getTime() > endTime) ? end.getTime() : endTime;
                }
            }
            
            // clean up information after 3 minutes
            if ((endTime + 180000) < now) {
                startMap.remove(sessionId);
                endMap.remove(sessionId);
            }
        }
    }


}
