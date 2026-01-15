package de.ipb_halle.lbac.authentication;

import de.ipb_halle.lbac.security.TokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/role")
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    TokenService tokenService;

    @GET
    public Response getRoleMessage(
            @HeaderParam("Authorization") String authHeader) {

        // 1️⃣ Check Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Missing or invalid Authorization header\"}")
                    .build();
        }

        // 2️⃣ Validate token
        String token = authHeader.substring("Bearer ".length());
        if (!tokenService.validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Invalid token\"}")
                    .build();
        }

        // 3️⃣ Check username
        String username = tokenService.getUsernameFromToken(token);

        String message;
        if ("admin".equals(username.toLowerCase())) {
            message = "You have admin access permissions.";
        } else {
            message = "You are a normal user with limited access.";
        }

        return Response.ok(
                "{"
                        + "\"username\":\"" + username + "\", "
                        + "\"token\":\"" + token + "\", "
                        + "\"message\":\"" + message + "\""
                        + "}")
                .build();
    }
}
