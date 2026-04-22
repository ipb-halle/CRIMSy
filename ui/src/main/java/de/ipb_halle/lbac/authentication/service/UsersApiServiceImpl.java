/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.UsersApiService;
import de.ipb_halle.lbac.admission.GroupEntity;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.admission.MembershipEntity;
import de.ipb_halle.lbac.admission.UserEntity;
import de.ipb_halle.lbac.security.interceptor.Secured;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.DeleteUser200Response;
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
import jakarta.transaction.Transactional;

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
                    "SELECT m FROM MemberEntity m WHERE m.login NOT LIKE '@%' AND TYPE(m) <> GroupEntity ORDER BY m.id",
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

        // --- Map userIds -> roles ---
        Map<Integer, List<String>> userRolesMap = new HashMap<>();

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

    // ----------------- Admin CRUD Operations -----------------
    @Override
    public Response createUser(UserSummary userSummary, SecurityContext securityContext) {
        if (!isAdmin(securityContext)) {
            return unauthorized();
        }

        if (userSummary == null || userSummary.getName() == null || userSummary.getEmail() == null) {
            return badRequest("Missing required fields");

        }
        List<UserEntity> duplicates = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.login = :login OR u.email = :email", UserEntity.class)
                .setParameter("login", userSummary.getName())
                .setParameter("email", userSummary.getEmail())
                .getResultList();
        if (!duplicates.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("User with this login or email already exists");
            errorResponse.setCode("409");
            return Response.status(Response.Status.CONFLICT).entity(errorResponse).build();
        }
        UserEntity newUser = new UserEntity();
        newUser.setName(userSummary.getName());
        newUser.setEmail(userSummary.getEmail());
        newUser.setLogin(userSummary.getName());
        newUser.setPassword("default"); // TODO: hash password
        em.persist(newUser);

        return Response.status(Response.Status.CREATED).entity(toDto(newUser)).build();
    }

    @Override
    @Transactional
    @Secured
    public Response deleteUser(Integer id, SecurityContext securityContext) {
/*
        if (!isAdmin(securityContext)) {
            return unauthorized();
        }*/
        if (id == null) {
            return badRequest("Invalid user ID");
        }

        UserEntity user = em.find(UserEntity.class, id);
        if (user == null) {
            return notFound("User not found");
        }

        List<MembershipEntity> memberships = em.createQuery(
                "SELECT ms FROM MembershipEntity ms WHERE ms.member = :userId", MembershipEntity.class)
                .setParameter("userId", id)
                .getResultList();
        memberships.forEach(em::remove);

        em.remove(user);

        DeleteUser200Response resp = new DeleteUser200Response();
        resp.setMessage("User deleted successfully");
        return Response.ok(resp).build();
    }

    @Override
    public Response updateUser(Integer id, UserSummary userSummary, SecurityContext securityContext) {
        if (!isAdmin(securityContext)) {
            return unauthorized();
        }
        if (id == null || userSummary == null) {
            return badRequest("Invalid input");
        }

        UserEntity user = em.find(UserEntity.class, id);
        if (user == null) {
            return notFound("User not found");
        }

        if (userSummary.getName() != null) {
            user.setName(userSummary.getName());
        }
        if (userSummary.getEmail() != null) {
            user.setEmail(userSummary.getEmail());
        }

        if (userSummary.getGroups() != null) {
            List<MembershipEntity> memberships = em.createQuery(
                    "SELECT ms FROM MembershipEntity ms WHERE ms.member = :userId", MembershipEntity.class)
                    .setParameter("userId", id)
                    .getResultList();
            memberships.forEach(em::remove);

            for (String gname : userSummary.getGroups()) {
                List<GroupEntity> groups = em.createQuery(
                        "SELECT g FROM GroupEntity g WHERE g.name = :name", GroupEntity.class)
                        .setParameter("name", gname)
                        .getResultList();
                if (!groups.isEmpty()) {
                    MembershipEntity ms = new MembershipEntity();
                    ms.setMember(id);
                    ms.setGroup(groups.get(0).getId());
                    em.persist(ms);
                }
            }
        }

        return Response.ok(toDto(user)).build();

    }

    // ----------- Helpers -----------
    private boolean isAdmin(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return false;
        }
        String username = securityContext.getUserPrincipal().getName();
        try {
            Integer memberId = em.createQuery(
                    "SELECT m.id FROM UserEntity m WHERE m.login = :login",
                    Integer.class)
                    .setParameter("login", username)
                    .getSingleResult();
            List<String> groups = em.createQuery(
                    "SELECT g.name FROM MembershipEntity ms, GroupEntity g "
                    + "WHERE ms.group = g.id AND ms.member = :memberID",
                    String.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
            return groups.stream().anyMatch(g -> g.equalsIgnoreCase("Admin Group"));
        } catch (NoResultException e) {
            return false;
        }
    }

    private Response unauthorized() {
        ErrorResponse err = new ErrorResponse();
        err.setMessage("Unauthorized");
        err.setCode("401");
        return Response.status(Response.Status.UNAUTHORIZED).entity(err).build();
    }

    private Response notFound(String msg) {
        ErrorResponse err = new ErrorResponse();
        err.setMessage(msg);
        err.setCode("404");
        return Response.status(Response.Status.NOT_FOUND).entity(err).build();
    }

    private Response badRequest(String msg) {
        ErrorResponse err = new ErrorResponse();
        err.setMessage(msg);
        err.setCode("400");
        return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
    }

    private UserSummary toDto(UserEntity u) {
        UserSummary s = new UserSummary();
        s.setId(u.getId());
        s.setName(u.getName());
        s.setEmail(u.getEmail());
        s.setGroups(fetchUserGroups(u.getId()));
        s.setAdmin(s.getGroups().stream().anyMatch(g -> g.equalsIgnoreCase("Admin Group")));
        return s;
    }

    private List<String> fetchUserGroups(Integer userId) {
        return em.createQuery(
                "SELECT g.name FROM MembershipEntity ms, GroupEntity g "
                + "WHERE ms.group = g.id AND ms.member = :userId", String.class)
                .setParameter("userId", userId)
                .getResultList();
    }

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
