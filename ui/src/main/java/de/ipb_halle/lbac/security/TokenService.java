/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author halocal
 */
@ApplicationScoped
public class TokenService {
    
    private Map<String, String> tokenStore = new ConcurrentHashMap<>();
    
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    //public String generateToken(String username) {
    public String generateToken(MemberEntity user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getName());

        // Save token in DB
        UserSessionsEntity session = new UserSessionsEntity(user, token);
        em.persist(session);
        
        return token;
    }
    
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }
    
    public String getUsernameFromToken(String token) {
        return tokenStore.get(token);
    }
    
    @Transactional
    public void revokeToken(String token) {
        tokenStore.remove(token);

        // Remove from DB
        UserSessionsEntity session = em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.token = :token", UserSessionsEntity.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElse(null);
        
        if (session != null) {
            em.remove(session);
        }
    }
}
