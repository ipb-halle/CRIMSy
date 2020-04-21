/*
 * CRIMSy 
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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.entity.FileObject;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.entity.SOPEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This is a very basic implementation of a Standard
 * Operating Procedure (SOP) - merely an id, a short 
 * description and a SOP document. This class may become 
 * a subclass of a <i>to be invented</i> class <code>Workflow</code>.
 * So far, owner, changelog, ACLs etc. are missing completely to 
 * keep things easy. 
 * 
 * SOPs will be referenced by assays. 
 *
 * @author fbroda
 */
public class SOP implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Integer     sopid;
    private String      description;
    private FileObject  document;

    public SOP(int sopid, 
            String description, 
            FileObject document) {
        this.description = description;
        this.document = document;
        this.sopid = sopid;
    }

    public SOPEntity createEntity() {
        return new SOPEntity()
                .setDescription(this.description)
                .setDocumentId(this.document.getId())
                .setSopId(this.sopid);
    }

    public String getDescription() {
        return this.description;
    }

    public FileObject getDocument() {
        return this.document;
    }

    public int getSopId() {
        return this.sopid;
    }

    public SOP setDescription(String description) {
        this.description = description;
        return this;
    }

    public SOP setDocument(FileObject document) {
        this.document = document;
        return this;
    }

    public SOP setSopId(int sopid) { 
        this.sopid = sopid; 
        return this;
    }
}
