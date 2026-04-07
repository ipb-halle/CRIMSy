/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.UsersApiService;
import de.ipb_halle.lbac.admission.GroupEntity;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.ErrorResponse;
import de.ipb_halle.model.PaginatedUserResponse;
import de.ipb_halle.model.UserSummary;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author halocal
 */
@RequestScoped
public class UsersApiServiceImpl implements UsersApiService {

    @Inject
    TokenService tokenService;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpHeaders headers;

    @Override
    public Response getUsersList(Integer page, Integer pageSize, SecurityContext securityContext) {

        // --- Validate pagination ---
        if (page < 1 || pageSize < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Invalid page or pageSize!"))
                    .build();
        }

        // --- Resolve username from security context ---
        String username = resolveUsername(securityContext);

        // --- Fallback: extract from Authorization header ---
        if (username == null) {
            return buildUnauthorized("Unauthorized: missing or invalid token");
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
            return buildError(Response.Status.NOT_FOUND, "User not found");
        }

        // --- Determine if requester is admin (where membertype = 'G') ---
        List<String> requesterGroups = em.createQuery(
                """
                        SELECT g.name
                        FROM MembershipEntity ms, MemberEntity g
                        WHERE ms.group = g.id 
                        AND ms.member = :memberId
                        AND TYPE(g) = GroupEntity
                        """, String.class)
                .setParameter("memberId", requestingUser.getId())
                .getResultList();

        boolean requesterIsAdmin = requesterGroups.stream()
                .anyMatch(a -> "Admin Group".equalsIgnoreCase(a));
        System.out.println("\n users.admin-stream: " + requesterIsAdmin + " \n");

        // --- Fetch users ---
        List<MemberEntity> users;
        int totalUsers = 0;

        if (requesterIsAdmin) {
            // Count total uses pagination 
            totalUsers = em.createQuery(
                    "SELECT COUNT(m) FROM MemberEntity m WHERE TYPE(m) <> GroupEntity",
                    Long.class)
                    .getSingleResult()
                    .intValue();

            int offset = (page - 1) * pageSize;

            // --- Fetch paginated users ---
            users = em.createQuery(
                    "SELECT m FROM MemberEntity m WHERE TYPE(m) <> GroupEntity ORDER BY m.id",
                    MemberEntity.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .getResultList();
        } else {
            // Non-admin: only own info
            users = List.of(requestingUser);
            totalUsers = 1;
            page = 1;
            pageSize = 1;
        }

        // --- Collect user IDs ---
        List<Integer> userIds = users.stream()
                .map(MemberEntity::getId)
                .collect(Collectors.toList());

        for (int i : userIds) {
            System.out.println("user id:: " + i + " \n");
        }

        List<Object[]> roleResults = Collections.emptyList();

// --- Fetch All roles for All users in one query ---
        if (!userIds.isEmpty()) {
            roleResults = em.createQuery(
                    """
                SELECT ms.member, g.name
                FROM MembershipEntity ms, MemberEntity g
                WHERE ms.group = g.id
                AND ms.member IN :userIds
                AND TYPE(g) = :groupType
                """, Object[].class)
                    .setParameter("userIds", userIds)
                    .setParameter("groupType", GroupEntity.class)
                    .getResultList();
        }
        System.out.println("roleResults: " + roleResults.size() + " \n");

        // --- Map userIds -> roles ---
        Map<Integer, List<String>> userRolesMap = new HashMap<>();

        System.out.println("useRolesMap: " + userRolesMap.size() + " \n");

        for (Object[] row : roleResults) {
            Integer userId = (Integer) row[0];
            String role = (String) row[1];

            userRolesMap.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(role);
        }

        // --- Build response ---
        List<UserSummary> responseUsers = users.stream()
                .filter(MemberEntity::isUser)
                .map(u -> {
                    UserSummary summary = new UserSummary();

                    summary.setId(u.getId());
                    summary.setName(u.getName());
                    summary.setEmail("");
                    //    summary.setGroups(u.isGroup() ? {"G"} : {"U"});
                    List<String> roles = userRolesMap.getOrDefault(u.getId(), Collections.emptyList());
                    summary.setGroups(roles);
                    boolean isAdmin = roles.stream()
                            .anyMatch(a -> "Admin Group".equalsIgnoreCase(a));
                    summary.setAdmin(isAdmin);
                    return summary;
                }).collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        PaginatedUserResponse response = new PaginatedUserResponse();

        response.setCurrentPage(page);
        response.setTotalItems(totalUsers);
        response.setTotalPages(totalPages);
        response.setItems(responseUsers);
        return Response.ok(response).build();
    }
    // ----------- Helpers -----------

    private String resolveUsername(SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring("Bearer ".length());

        if (!tokenService.validateToken(token, false)) {
            return null;
        }
        return tokenService.getUsernameFromToken(token);
    }

    private Response buildUnauthorized(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(message);
        error.setCode("401");
        return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
    }

    private Response buildError(Response.Status status, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(message);
        error.setCode(String.valueOf(status.getStatusCode()));
        return Response.status(status).entity(error).build();
    }

}
