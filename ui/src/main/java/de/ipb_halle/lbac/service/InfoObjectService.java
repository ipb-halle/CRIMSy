/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.service;

import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.entity.InfoObjectEntity;


import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

@Stateless
public class InfoObjectService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ACListService aclistService;

    @Inject
    private MemberService memberService;

    private Logger logger;

    public InfoObjectService() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Obtain the InfoObject for the given key
     *
     * @param key - String key
     * @return InfoObject (key, value)
     */
    public InfoObject loadByKey(String key) {

        InfoObjectEntity entity = this.em.find(InfoObjectEntity.class, key);
        if (entity == null) {
            logger.warn("No info object found for key: " + key);
            return null;
        }

        // NOTE: "DBSchema Version" has no owner and no ACList
        if ((entity.getACList() == null) || (entity.getOwner() == null)) {
            return new InfoObject(entity, null, null);
        }

        return new InfoObject(
                entity,
                aclistService.loadById(entity.getACList()),
                memberService.loadUserById(entity.getOwner()));

    }

    /**
     * save info entity (key,value) to table info
     *
     * @param infoObject
     * @return
     */
    public InfoObject save(InfoObject infoObject) {
        try {
            infoObject.setACList(this.aclistService.save(infoObject.getACList()));
            return new InfoObject(
                    this.em.merge(infoObject.createEntity()),
                    infoObject.getACList(),
                    infoObject.getOwner());
        } catch (Exception e) {
            logger.warn("save() failed: ", e);
        }
        return null;
    }
}
