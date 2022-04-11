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
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author fbroda
 */
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
     * Loads available reports for the given context from the database. The report
     * names are assigned according to the given language.
     * 
     * @param context  the reporting context (the name of the class)
     * @param language the ISO639 specification of the language
     * @return map of ids and names of reports
     */
    public Map<Integer, String> load(String context, String language) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ReportEntity> criteriaQuery = builder.createQuery(ReportEntity.class);
        Root<ReportEntity> reportRoot = criteriaQuery.from(ReportEntity.class);
        criteriaQuery.select(reportRoot);
        criteriaQuery.where(builder.equal(reportRoot.get("context"), context));

        Map<Integer, String> results = new HashMap<>();
        for (ReportEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            String name = DEFAULT_NAME;
            try {
                name = extractNameFromEntity(entity, language);
            } catch (JsonSyntaxException e) {
                logger.error("Failed to obtain localized [{}] name for report [{}]: {}", language, entity.getId(),
                        e.getMessage());
            }
            results.put(entity.getId(), name);
        }

        return results;
    }

    private String extractNameFromEntity(ReportEntity entity, String language) throws JsonSyntaxException {
        String json = entity.getName();
        JsonElement jsonTree = JsonParser.parseString(json);

        String name = jsonTree.getAsJsonObject().get(language).getAsString();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        // resort to name in default language
        name = jsonTree.getAsJsonObject().get(DEFAULT_LANGUAGE).getAsString();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        logger.warn("Default localization missing for report with id={}", entity.getId());
        return DEFAULT_NAME;
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
