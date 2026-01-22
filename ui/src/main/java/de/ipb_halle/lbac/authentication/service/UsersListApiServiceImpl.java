/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.UsersListApiService;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.GetRoleInfo401Response;
import de.ipb_halle.model.GetRoleInfo404Response;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author halocal
 */
@RequestScoped
public class UsersListApiServiceImpl implements UsersListApiService {
    @Inject
    TokenService tokenService;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpHeaders headers;

    @Override
    public Response usersListGet(SecurityContext securityContext) {

        String username = null;

        // 1. First, try SecurityContext
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            username = securityContext.getUserPrincipal().getName();
        }

        // 2. Fallback: extract from Authorization header
        if (username == null) {
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                GetRoleInfo401Response resp = new GetRoleInfo401Response();
                resp.setMessage("Unauthorized: missing token");
                return Response.status(Response.Status.UNAUTHORIZED).entity(resp).build();
            }
            String token = authHeader.substring("Bearer ".length());
            if (!tokenService.validateToken(token)) {
                GetRoleInfo401Response resp = new GetRoleInfo401Response();
                resp.setMessage("Unauthorized: invalid token");
                return Response.status(Response.Status.UNAUTHORIZED).entity(resp).build();
            }
            username = tokenService.getUsernameFromToken(token);
        }

        // --- Load requesting user ---
        MemberEntity requestingUser;
        try {
            requestingUser = em.createQuery(
                    "SELECT m FROM MemberEntity m WHERE m.login = :login",
                    MemberEntity.class)
                    .setParameter("login", username.toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            GetRoleInfo404Response resp = new GetRoleInfo404Response();
            resp.setMessage("User not found");
            return Response.status(Response.Status.NOT_FOUND).entity(resp).build();
        }

        // --- Check groups where membertype = 'G' to see if admin ---
        List<String> userGroups = em.createQuery(
                """
                        SELECT g.name
                        FROM MembershipEntity ms
                        JOIN MemberEntity g ON ms.group = g.id
                        WHERE ms.member = :memberId
                        AND TYPE(g) = GroupEntity
                        """, String.class)
                .setParameter("memberId", requestingUser.getId())
                .getResultList();

        boolean isAdmin = userGroups.stream()
                .anyMatch(g -> "Admin Group".equalsIgnoreCase(g));
        // --- Fetch users ---
        List<MemberEntity> users;
        if (isAdmin) {
            // Admin: fetch all users (exclude groups)
            users = em.createQuery(
                    "SELECT m FROM MemberEntity m WHERE TYPE(m) <> GroupEntity",
                    MemberEntity.class)
                    .getResultList();
        } else {
            // Non-admin: only own info
            users = List.of(requestingUser);
        }
        // --- Map to API response ---
        List<Object> responseList = users.stream().map(u -> {
            return new java.util.HashMap<String, Object>() {
                {
                    put("id", u.getId());
                    put("login", u.getName()); // or u.getLogin() if you have that field
                    put("name", u.getName());
                    put("membertype", u.isGroup() ? "G" : "U");
                    if (!isAdmin) {
                        put("info", "You are not an Admin, only your own info is shown");
                    }
                }
            };
        }).collect(Collectors.toList());
        return Response.ok(responseList).build();
    }
}