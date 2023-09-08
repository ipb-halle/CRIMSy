/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.tx.test;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fbroda
 */
@Stateless
public class EntityManagerService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    public EntityManager getEntityManager() {
        return em;
    }
}
