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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class TissueService {

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private final String SQL_LOAD_TISSUES = "SELECT id,taxoid FROM tissues";
    private final String SQL_LOAD_TISSUES_CONTRAINED
            = "SELECT DISTINCT t.id,t.taxoid "
            + "FROM tissues t "
            + "JOIN effective_taxonomy et ON et.parentid=t.taxoid "
            + "WHERE et.taxoid=:taxoid OR et.parentid=:taxoid";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @SuppressWarnings("unchecked")
    public List<Tissue> loadTissues() {
        List<Tissue> tissues = new ArrayList<>();
        List<TissueEntity> entities = this.em.createNativeQuery(SQL_LOAD_TISSUES, TissueEntity.class).getResultList();
        for (TissueEntity entity : entities) {
            tissues.add(
                    new Tissue(entity.getId(),
                            materialService.loadMaterialNamesById(entity.getId()),
                            taxonomyService.loadTaxonomyById(entity.getId()))
            );
        }
        return tissues;
    }

    @SuppressWarnings("unchecked")
    public List<Tissue> loadTissues(Taxonomy targetTaxo) {
        List<Tissue> tissues = new ArrayList<>();
        List<TissueEntity> entities = this.em.createNativeQuery(SQL_LOAD_TISSUES_CONTRAINED, TissueEntity.class)
                .setParameter("taxoid", targetTaxo.getId())
                .getResultList();
        for (TissueEntity entity : entities) {
            Map<String, Object> cmap = new HashMap<>();
            cmap.put("id", entity.getTaxoid());
            Taxonomy t = taxonomyService.loadTaxonomy(cmap, true).get(0);
            tissues.add(
                    new Tissue(entity.getId(),
                            materialService.loadMaterialNamesById(entity.getId()),
                            t)
            );
        }
        return tissues;
    }

    public Tissue loadTissueById(int id) {
        TissueEntity entity = this.em.find(TissueEntity.class, id);

        Tissue t = new Tissue(
                entity.getId(),
                materialService.loadMaterialNamesById(entity.getId()),
                taxonomyService.loadTaxonomyById(entity.getId())
        );

        return t;

    }
}
