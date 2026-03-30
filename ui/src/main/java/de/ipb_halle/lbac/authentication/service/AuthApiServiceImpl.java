package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.AuthApiService;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.security.interceptor.Secured;
import de.ipb_halle.lbac.security.service.SessionService;
import de.ipb_halle.lbac.security.service.TokenService;

import de.ipb_halle.model.AuthToken;
import de.ipb_halle.model.AuthUser;
import de.ipb_halle.model.ErrorResponse;
import de.ipb_halle.model.LoginRequest;
import de.ipb_halle.model.LogoutResponse;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import java.util.List;

/**
 *
 * @author halocal
 */
public class AuthApiServiceImpl implements AuthApiService {

    @Inject
    private TokenService tokenService;

    @Inject
    LogInProcess loginProcess;

    @Inject
    SessionService sessionService;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpHeaders headers;

    @Override
    public Response login(LoginRequest loginRequest, SecurityContext securityContext) {

        String ipAddressString = getClientIp();

        User user = loginProcess.tryLogIn(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                ipAddressString);

        if (user == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Login failed: invalid credentials!");
            errorResponse.setCode("401");
            return Response.status(
                    Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }

        MemberEntity member;
        try {
            member = em.createQuery(
                    "SELECT m FROM MemberEntity m WHERE m.login = :login",
                    MemberEntity.class)
                    .setParameter("login", user.getLogin())
                    .getSingleResult();
        } catch (NoResultException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("User not found");
            errorResponse.setCode("500");
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }

        String token = tokenService.generateToken(member);
        if (token == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid credentials");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }

        AuthToken response = new AuthToken();
        response.setToken(token);
        response.setExpiresInSeconds(60);
        return Response.ok(response).build();
    }

    @Override
    @Transactional
    @Secured
    public Response logout(SecurityContext securityContext) {

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Missing or invalid Authorization header");
            error.setCode("401");
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }

        String token = authHeader.substring("Bearer ".length());
        sessionService.deleteSessionByToken(token);

        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setMessage("Logout Successfully");
        return Response.ok(logoutResponse).build();
    }

    @Override
    @Secured
    public Response getCurrentUser(SecurityContext securityContext) {
        String username = null;

        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            username = securityContext.getUserPrincipal().getName();
        }

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unauthorized: missing token");
            error.setCode("401");
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }
        String token = authHeader.substring("Bearer ".length());
        if (!sessionService.isTokenValid(token, true)) {
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unauthorized: invalid token");
            error.setCode("401");
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }

        username = tokenService.getUsernameFromToken(token);
        MemberEntity member;
        try {
            member = em.createQuery(
                    "SELECT m FROM MemberEntity m WHERE m.login = :login",
                    MemberEntity.class)
                    .setParameter("login", username.toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            ErrorResponse error = new ErrorResponse();
            error.setMessage("User not found");
            error.setCode("404");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        List<String> groups = em.createQuery(
                """
                        SELECT g.name
                        FROM MembershipEntity ms
                        JOIN MemberEntity g ON ms.group = g.id
                        WHERE ms.member = :memberId
                        AND TYPE(g) = GroupEntity
                        """, String.class)
                .setParameter("memberId", member.getId())
                .getResultList();

        boolean isAdmin = groups.stream()
                .anyMatch(g -> "Admin Group".equalsIgnoreCase(g));
        
        AuthUser authUserResponse = new AuthUser();
        authUserResponse.setId(member.getId());
        authUserResponse.setUsername(username);
        authUserResponse.setName(member.getName());
        authUserResponse.setGroups(groups);
        authUserResponse.setAdmin(isAdmin);
        
        return Response.ok(authUserResponse).build();

    }

    public String getClientIp() {
        String ipAddressString = headers.getHeaderString("X-FORWARDED-FOR");
        if (ipAddressString != null && !ipAddressString.isEmpty()) {
            return ipAddressString.split(",")[0].trim();
        }
        return null;
    }
}
