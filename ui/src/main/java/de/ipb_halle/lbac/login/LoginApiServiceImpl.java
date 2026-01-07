/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.login;

import de.ipb_halle.api.LoginApiService;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.security.TokenService;
import de.ipb_halle.model.LoginRequest;
import de.ipb_halle.model.LoginResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 *
 * @author halocal
 */
public class LoginApiServiceImpl implements LoginApiService {

    @Inject
    LogInProcess loginProcess;

    @Inject
    TokenService tokenService;
    
    @PersistenceContext
    private  EntityManager em;

    @Override
    public Response login(LoginRequest loginRequest, SecurityContext securityContext) {

        String login = loginRequest.getLogin();
        String password = loginRequest.getPassword();
        User user = loginProcess.tryLogIn(login, password);

        if (user != null) {

//            String token = tokenService.generateToken(user.getLogin());

            MemberEntity memberEntity;
            
            try{
                memberEntity = em.createQuery(
                        "SELECT m FROM MemberEntity m WHERE m.login = :login", MemberEntity.class)
                        .setParameter("login", user.getLogin())
                        .getSingleResult();
            } catch(NoResultException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\\\"message\\\":\\\"User entity not found in database!\\\"}")
                        .build();
            }
            
            String token = tokenService.generateToken(memberEntity);

            LoginResponse response = new LoginResponse();
            response.setMessage("Login Succedd!");
            response.setUsername(user.getLogin());
            response.setToken(token);

            return Response.status(Response.Status.OK)
                    .entity(response)
                    .build();
        } else {

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Login failed\"}")
                    .build();
        }
    }
}
