/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security.job;

import de.ipb_halle.lbac.security.service.SessionService;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/**
 *
 * @author halocal
 */
@Singleton
@Startup
public class SessionCleanupJob {

    @Inject
    private SessionService sessionService;

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredSessions() {
        sessionService.deleteExpiredSessions();
    }
}
