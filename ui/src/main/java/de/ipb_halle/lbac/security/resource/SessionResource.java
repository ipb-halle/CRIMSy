/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security.resource;

import de.ipb_halle.lbac.security.service.SessionService;
import de.ipb_halle.lbac.security.service.TokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author halocal
 */
@Path("sessions")
@ApplicationScoped
public class SessionResource {

    @Inject
    SessionService sessionService;

    @Inject
    TokenService tokenService;

    @OPTIONS
    public Response options() {
        return Response.ok().build();
    }

    @POST
    @Transactional
    public Response refreshSession(
            @HeaderParam("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        if (!tokenService.validateToken(token, true)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        boolean updated = sessionService.updateSession(token);

        return updated
                ? Response.ok().build()
                : Response.status(Response.Status.UNAUTHORIZED).build();
    }

}
