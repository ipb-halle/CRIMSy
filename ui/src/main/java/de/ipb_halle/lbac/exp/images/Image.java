/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.plugin.imageAnnotation.DataURIImage;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class Image extends ExpRecord implements Serializable {

    protected User user;
    @DataURIImage
    protected String preview;
    protected String image;
    protected ACList aclist;

    public Image(
            ImageEntity entity,
            ACList aclist,
            User user) {
        this.user = user;
        this.aclist = aclist;
        this.preview = entity.getPreview();
        this.image = entity.getImage();
        this.setExpRecordId(entity.getId());

        setType(ExpRecordType.IMAGE);
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

        setType(ExpRecordType.IMAGE);
    }

    public String getPreview() {
        return preview;
    }

    public String getImage() {
        return image;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public Long getId() {
        return getExpRecordId();
    }

    @Override
    public ImageEntity createEntity() {
        ImageEntity entity = new ImageEntity();
        entity.setACList(aclist.getId());
        entity.setId(getExpRecordId());
        entity.setImage(image);
        entity.setOwner(user.getId());
        entity.setPreview(preview);
        return entity;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public Set<ValidationError> getErrors() {
        return new HashSet<>();
    }

}
