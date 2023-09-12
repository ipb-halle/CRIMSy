/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.reporting.report;

import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author fbroda
 */
@Stateless
public class ReportService {
    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    /**
     * Loads available reports for the given context from the database.
     *
     * @param context the reporting context (the name of the class)
     * @return list of {@link Report} DTOs
     */
    public List<Report> loadByContext(String context) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ReportEntity> criteriaQuery = builder.createQuery(ReportEntity.class);
        Root<ReportEntity> reportRoot = criteriaQuery.from(ReportEntity.class);
        criteriaQuery.select(reportRoot);
        criteriaQuery.where(builder.equal(reportRoot.get("context"), context));

        List<Report> results = new ArrayList<>();
        for (ReportEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            results.add(new Report(entity));
        }

        return results;
    }

    public Report loadById(Integer id) {
        return new Report(em.find(ReportEntity.class, id));
    }
}
