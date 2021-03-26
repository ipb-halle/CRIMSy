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
package de.ipb_halle.lbac.exp.images;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.exp.Experiment;
import java.io.Serializable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class ImageService implements Serializable {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ACListService aclistService;

    @Inject
    private MemberService memberService;

    private static final long serialVersionUID = 1L;

    public Image saveImage(Image image) {
        if (image.id == null) {
            return saveNewImage(image);
        } else {
            return saveEditedImage(image);
        }
    }
    
    public Image loadImage(Experiment exp,ExpRecordEntity record) {
        ImageEntity imageEntity = em.find(ImageEntity.class, record.getExpRecordId().intValue());
        if (imageEntity == null) {
            return null;
        }
        Image image = new Image(
                imageEntity,
                aclistService.loadById(imageEntity.getACList()),
                memberService.loadUserById(imageEntity.getOwner()));
        image.setExpRecordEntity(record);
        image.setExperiment(exp);
        return image;
    }

    private Image saveNewImage(Image image) {
        ImageEntity entity = image.createEntity();
        em.persist(entity);
        image.id = entity.getId();
        return image;
    }

    private Image saveEditedImage(Image image) {
        em.merge(image.createEntity());
        return image;
    }

}
