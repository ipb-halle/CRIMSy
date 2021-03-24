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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.DTO;

/**
 *
 * @author fmauz
 */
public class Image implements DTO<ImageEntity> {

    protected User user;
    protected String preview;
    protected String image;
    protected ACList aclist;
    protected Integer id;

    public Image(
            ImageEntity entity,
            ACList aclist,
            User user) {
        this.user = user;
        this.aclist = aclist;
        this.preview = entity.getPreview();
        this.image = entity.getImage();
        this.id = entity.getId();
    }

    public Image(
            String preview,
            String image,
            User user,
            ACList aclist) {
        this.preview = preview;
        this.image = image;
        this.aclist = aclist;
        this.user = user;
    }

    public String getPreview() {
        return preview;
    }

    public String getImage() {
        return image;
    }

    @Override
    public ImageEntity createEntity() {
        ImageEntity entity = new ImageEntity();
        entity.setACList(aclist.getId());
        entity.setId(id);
        entity.setImage(image);
        entity.setOwner(user.getId());
        entity.setPreview(preview);
        return entity;
    }

}
