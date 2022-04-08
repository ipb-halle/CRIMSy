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
package de.ipb_halle.lbac.util.reporting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class ReportService implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static String DEFAULT_LANGUAGE = "en";
    private final static String DEFAULT_NAME = "REPORT";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public ReportService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * @param context the reporting context
     * @param language the ISO639 specification of the language 
     * @return a list of SelectItems for the given context
     */
    @SuppressWarnings("unchecked")
    public List<SelectItem> load(String context, String language) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<ReportEntity> criteriaQuery = builder.createQuery(ReportEntity.class);
        Root<ReportEntity> reportRoot = criteriaQuery.from(ReportEntity.class);
        criteriaQuery.select(reportRoot);
        criteriaQuery.where(builder.equal(reportRoot.get("context"), context)); 

        List<SelectItem> results = new ArrayList<> ();
        for(ReportEntity entity: this.em.createQuery(criteriaQuery).getResultList()) {

            String name = DEFAULT_NAME;
            try {
                JsonElement jsonTree = JsonParser.parseString(entity.getName());
                name = jsonTree.getAsJsonObject().get(language).getAsString();
                if (name == null) {
                    // resort to name in default locale
                    name = jsonTree.getAsJsonObject().get(DEFAULT_LANGUAGE).getAsString();
                }
                if (name == null) {
                    throw new Exception("Default localization missing");
                }
            } catch(Exception e) {
                this.logger.info("Failed to obtain localized [{}] name for report [{}]: {}", language, entity.getId(), e.getMessage());
                name = DEFAULT_NAME;
            }

            results.add(new SelectItem(entity.getId(), name));
        }
        return results;
    }

    /**
     * load a report by id
     *
     * @param id report Id
     * @return the Report object
     */
    public Report loadById(Integer id) {
        ReportEntity entity = this.em.find(ReportEntity.class, id);
        if (entity != null) {
            return new Report(entity);
        }
        return null;
    }
}
