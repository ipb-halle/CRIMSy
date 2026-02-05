/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import java.util.List;
import java.util.Map;

import de.ipb_halle.api.AuthApiService;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.security.interceptor.Secured;
import de.ipb_halle.lbac.security.service.SessionService;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.AuthResponse;
import de.ipb_halle.model.ErrorResponse;
import de.ipb_halle.model.LoginRequest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;

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
                loginRequest.getLogin(),
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
                    MemberEntity.class).setParameter("login", user.getLogin())
                    .getSingleResult();
        } catch (NoResultException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("User entity not found");
            errorResponse.setCode("500");
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }

        String token = tokenService.generateToken(member);

        AuthResponse response = new AuthResponse();

        response.setUsername(user.getLogin());
        response.setMessage("Logged in Successfully " + user.getLogin());
        response.setToken(token);
        response.setExpiresInSeconds(60);
        return Response.ok(response).build();
    }

    @Secured
    @Override
    @Transactional
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

        Map<String, String> message = Map.of("message", "Logged out successfully!");
        return Response.ok(message).build();
    }

    public String getClientIp() {
        String ipAddressString = headers.getHeaderString("X-FORWARDED-FOR");
        if (ipAddressString != null && !ipAddressString.isEmpty()) {
            return ipAddressString.split(",")[0].trim();
        }
        return null;
    }

}
