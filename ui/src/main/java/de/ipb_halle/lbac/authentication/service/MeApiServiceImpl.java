package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.MeApiService;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.ErrorResponse;
import de.ipb_halle.model.RoleResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class MeApiServiceImpl implements MeApiService {

    @Inject
    TokenService tokenService;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpHeaders headers;

    @Override
    public Response getRoleInfo(SecurityContext securityContext) {

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

        // --- Load member entity ---
        MemberEntity member;
        try {
            member = em.createQuery(
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

        // --- Fetch groups where membertype = 'G' ---
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

        boolean isAdmin = groups.stream().anyMatch(g -> "Admin Group".equalsIgnoreCase(g));

        RoleResponse response = new RoleResponse();
        response.setUsername(username);
        response.setGroups(groups);
        response.setAdmin(isAdmin);

        return Response.ok(response).build();
    }
}
