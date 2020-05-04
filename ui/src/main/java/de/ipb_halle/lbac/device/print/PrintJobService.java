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
 * PrintJobService loads, stores and deletes print jobs.
 */
import de.ipb_halle.lbac.service.MemberService;

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
public class PrintJobService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MemberService memberService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public PrintJobService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void PrintJobServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * This needs to be complemented by means to select:
     * <ul>
     * <li>print jobs by state (PENDING, FAILED, ...)</li>
     * <li>print jobs by printer</li>
     * <li>print jobs by owner?</li>
     * </ul>
     * @return the the complete list of PrintJobs
     */
    @SuppressWarnings("unchecked")
    public List<PrintJob> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<PrintJobEntity> criteriaQuery = builder.createQuery(PrintJobEntity.class);
        Root<PrintJobEntity> printerRoot = criteriaQuery.from(PrintJobEntity.class);
        criteriaQuery.select(printerRoot);
        List<PrintJob> result = new ArrayList<>();
        for (PrintJobEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new PrintJob(
                    e,
                    memberService.loadUserById(e.getOwnerId())));
        }
        return result;
    }

    /**
     * load a PrintJob by id 
     *
     * @param id PrintJob Id 
     * @return the PrintJob object
     */
    public PrintJob loadById(Integer id) {
        PrintJobEntity entity = this.em.find(PrintJobEntity.class, id);
        return new PrintJob(
                entity, 
                memberService.loadUserById(entity.getOwnerId()));
    }

    /**
     * remove a print job from the database
     * @param p the print job DTO
     */
    public void remove(PrintJob p) {
        this.em.remove(this.em.find(PrintJobEntity.class, p.getJobId()));
    }

    /**
     * save a single printer object
     *
     * @param p the PrintJob  to save
     * @return the persisted PrintJob DTO
     */
    public PrintJob save(PrintJob p) {
        return new PrintJob(
                this.em.merge(p.createEntity()),
                p.getOwner());
    }
}
