/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 *
 * @author halocal
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    TokenService tokenService;

    @Inject
    SessionService sessionService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Missing or invalid Authorization header\"}")
                    .build());
            return;
        }

        String token = authHeader.substring("Bearer ".length());
          if (!tokenService.validateToken(token)) {
//        if (!sessionService.isTokenValid(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Invalid token\"}")
                    .build());
        }
        sessionService.updateSession(token);
    }
}
