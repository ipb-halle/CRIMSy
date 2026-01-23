/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import java.util.List;

import de.ipb_halle.api.AuthApiService;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.security.service.SessionService;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.LoginRequest;
import de.ipb_halle.model.LoginResponse;
import de.ipb_halle.model.Logout200Response;
import de.ipb_halle.model.Logout401Response;
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

                LoginResponse response = new LoginResponse();

                User user = loginProcess.tryLogIn(
                                loginRequest.getLogin(),
                                loginRequest.getPassword());

                if (user == null) {
                        response.setMessage("Login failed");
                        return Response.status(Response.Status.UNAUTHORIZED)
                                        .entity(response)
                                        .build();
                }

                MemberEntity member;
                try {
                        member = em.createQuery(
                                        "SELECT m FROM MemberEntity m WHERE m.login = :login",
                                        MemberEntity.class).setParameter("login", user.getLogin())
                                        .getSingleResult();
                } catch (NoResultException e) {
                        response.setMessage("User entity not found");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(response)
                                        .build();
                }

                String token = tokenService.generateToken(member);

                response.setMessage("Logged in successfully, " + user.getLogin() + "!");
                response.setUsername(user.getLogin());
                response.setToken(token);
                response.setExpiresInSeconds(60);
                return Response.ok(response).build();
        }

        @Override
        @Transactional
        public Response logout(SecurityContext securityContext) {

                String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return Response.status(Response.Status.UNAUTHORIZED)
                                        .entity(new Logout401Response()
                                                        .message("Unauthorized: missing or invalid token"))
                                        .build();
                }

                String token = authHeader.substring("Bearer ".length());

                sessionService.deleteSessionByToken(token);
                Logout200Response response = new Logout200Response();
                response.setMessage("Logged out successfully");
                return Response.ok(response).build();
        }

}
