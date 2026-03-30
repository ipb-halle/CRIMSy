package de.ipb_halle.lbac.security.filter;

import de.ipb_halle.lbac.security.interceptor.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.Principal;

import de.ipb_halle.lbac.security.service.SessionService;
import de.ipb_halle.lbac.security.service.TokenService;
import jakarta.ws.rs.HttpMethod;

@Provider
@Secured
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    TokenService tokenService;

    @Inject
    SessionService sessionService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();
        String methoString = requestContext.getMethod();
        if(HttpMethod.OPTIONS.equalsIgnoreCase(methoString) || path.equals("auth/login")||  path.equals("auth/logout")) {
            return;
        }
        
        String authHeader = requestContext.getHeaderString("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Missing or invalid Authorization header\"}")
                    .build());
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        if (!tokenService.validateToken(token, false)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Invalid token\"}")
                    .build()
            );
            return;
        }

        final String username = tokenService.getUsernameFromToken(token);

        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return requestContext.getUriInfo().getAbsolutePath().getScheme().equals("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }
}
