package de.ipb_halle.lbac.service;

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserSessionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createSession(MemberEntity user, String token) {
        UserSessionsEntity sessionsEntity = new UserSessionsEntity(user, token);
        em.persist(sessionsEntity);
    }

    @Transactional
    public void updateLastSeen(String token) {
        UserSessionsEntity sessionsEntity = em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.token = :token",
                UserSessionsEntity.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (sessionsEntity != null) {
            sessionsEntity.setLastSeen(java.time.LocalDateTime.now());
            em.merge(sessionsEntity);
        }
    }

}
