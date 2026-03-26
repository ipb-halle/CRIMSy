/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security.service;

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 *
 * @author halocal
 */
@ApplicationScoped
public class TokenService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    SessionService sessionService;

    @Transactional
    public String generateToken(MemberEntity user) {
        String token = UUID.randomUUID().toString();
        UserSessionsEntity session = new UserSessionsEntity(user, token);
        em.persist(session);
        return token;
    }

    // for read only case
    // if there exists a transaction, it joins it, otherwise, it creates new one
    @Transactional(Transactional.TxType.SUPPORTS)
    public String getUsernameFromToken(String token) {
        String username = null;
        UserSessionsEntity userSessionsEntity = sessionService.getUserSessionsEntityByToken(token);
        if (userSessionsEntity != null) {
            username = userSessionsEntity.getUser().getName();
        }
        return username;
    }
    
    
     @Transactional
     public boolean validateToken(String token, boolean tokenExpired) {
         return !sessionService.isTokenExpired(token, tokenExpired);
     }
}
