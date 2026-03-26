/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security.interceptor;

import de.ipb_halle.lbac.security.service.TokenService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author halocal
 */
@Interceptor
@Secured
public class SecurityInterceptor {

    @Inject
    private TokenService tokenService;

    @Context
    @Inject
    private HttpHeaders headers;

    @AroundInvoke
    public Object checkToken(InvocationContext invocationContext) throws Exception {

        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Missing or invalid Auhtorization header\"}")
                    .build();
        }
        String token = authHeader.substring("Bearer ".length());

        boolean validatedToken = tokenService.validateToken(token, false);
        if (!validatedToken) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Invalid or expired token\"}")
                    .build();
        }
        // token is valid -> proceed to the actual endpoint
        return invocationContext.proceed();
    }
}
