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

    @PersistenceContext
    private EntityManager em;

    @Transactional
    //public String generateToken(String username) {
    public String generateToken(MemberEntity user) {
        String token = UUID.randomUUID().toString();

        // Save token in DB
        UserSessionsEntity session = new UserSessionsEntity(user, token);
        em.persist(session);
        return token;
    }

    // for read only case 
    // if there exists a transaction, it joins it, otherwise, it creates new one 
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean validateToken(String token) {
        return em.createQuery(
                "SELECT COUNT(s) FROM UserSessionsEntity s WHERE s.token = :token",
                Long.class)
                .setParameter("token", token)
                .getSingleResult() > 0;
    }

    // for read only case
    // if there exists a transaction, it joins it, otherwise, it creates new one
    @Transactional(Transactional.TxType.SUPPORTS)
    public String getUsernameFromToken(String token) {
        String username = null;
        UserSessionsEntity userSessionsEntity
                = em.createQuery(
                        "SELECT s FROM UserSessionsEntity s WHERE s.token = :token",
                        UserSessionsEntity.class)
                        .setParameter("token", token)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
        if (userSessionsEntity != null) {
            username = userSessionsEntity.getUser().getName();
        }
        return username;
    }

    @Transactional
    public void revokeToken(String token) {

        // Remove from DB
        em.createQuery(
                "DELETE FROM UserSessionsEntity s WHERE s.token = :token")
                .setParameter("token", token)
                .executeUpdate();
        /*UserSessionsEntity session = em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.token = :token", UserSessionsEntity.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElse(null);
        
        if (session != null) {
            em.remove(session);
        }*/
    }
}
