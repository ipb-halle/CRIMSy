/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.UsersApiService;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.ErrorResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /*@Override
    public Response usersListGet(Integer page, Integer pageSize, SecurityContext securityContext) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }*/
    @Override
    public Response usersGet(Integer page, Integer pageSize, SecurityContext securityContext) {

        if (page < 1 || pageSize < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Invalid page or pageSize!"))
                    .build();
        }

        String username = null;

        // 1. First, try SecurityContext
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            username = securityContext.getUserPrincipal().getName();
        }

        // 2. Fallback: extract from Authorization header
        if (username == null) {
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Unauthorized: missing token");
                errorResponse.setCode("401");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(errorResponse)
                        .build();
            }
            String token = authHeader.substring("Bearer ".length());

            if (!tokenService.validateToken(token)) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Unauthorized: invalid token");
                errorResponse.setCode("401");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(errorResponse)
                        .build();
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
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("User not found");
            errorResponse.setCode("404");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
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
        int totalUsers = 0;

        if (isAdmin) {
            // Count total uses pagination 
            totalUsers = em.createQuery(
                    "SELECT COUNT(m) FROM MemberEntity m WHERE TYPE(m) <> GroupEntity",
                    Long.class)
                    .getSingleResult()
                    .intValue();

            int offset = (page - 1) * pageSize;

            // Fetch paginated users
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

        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        // --- Map uers to API response ---
        List<Map<String, Object>> responseUsersList = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("membertype", u.isGroup() ? "G" : "U");
            if (!isAdmin) {
                map.put("info", "You are not an Admin, only your own info is shown");
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> responsesMap = new HashMap<>();
        responsesMap.put("totalUsers", totalUsers);
        responsesMap.put("totalPages", totalPages);
        responsesMap.put("currentPage", page);
        responsesMap.put("users", responseUsersList);

        return Response.ok(responsesMap).build();
    }

}
