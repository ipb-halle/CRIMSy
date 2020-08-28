/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.device.print;

/**
 * PrinterService loads and stores printers.
 *
 */
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class PrinterService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ACListService aclistService;

    @Inject
    private MemberService memberService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public PrinterService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void PrinterServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * This needs to be complemented by means to select:
     * <ul>
     * <li>printers a user can print to</li>
     * <li>printers a user is allowed to manage</li>
     * <li>printers in a certain state (ready, failed, ...)</li>
     * </ul>
     * @return the the complete list of Printers
     */
    @SuppressWarnings("unchecked")
    public List<Printer> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<PrinterEntity> criteriaQuery = builder.createQuery(PrinterEntity.class);
        Root<PrinterEntity> printerRoot = criteriaQuery.from(PrinterEntity.class);
        criteriaQuery.select(printerRoot);
        List<Printer> result = new ArrayList<>();
        for (PrinterEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Printer(
                    e,
                    aclistService.loadById(e.getACListId()),
                    memberService.loadUserById(e.getOwnerId())));
        }
        return result;
    }

    /**
     * load a Printer by name
     *
     * @param queue Printer queue
     * @return the Printer object
     */
    public Printer loadById(String queue) {
        PrinterEntity entity = this.em.find(PrinterEntity.class, queue);
        return new Printer(
                entity, aclistService.loadById(entity.getACListId()),
                memberService.loadUserById(entity.getOwnerId()));
    }

    /**
     * delete a Printer from the system
     * @param p the printer to be deleted
     */
    public void delete(Printer p) {
        PrinterEntity entity = this.em.find(PrinterEntity.class, p.getQueue());
        this.em.remove(entity);
    }

    /**
     * save a single printer object
     *
     * @param p the Printer  to save
     * @return the persisted Printer DTO
     */
    public Printer save(Printer p) {
        return new Printer(
                this.em.merge(p.createEntity()),
                p.getACList(),
                p.getOwner());
    }
}
