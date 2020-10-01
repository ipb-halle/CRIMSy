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
package de.ipb_halle.lbac.collections.mock;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.FileService;

/**
 *
 * @author fmauz
 */
public class CollectionBeanMock extends CollectionBean {

    public CollectionBeanMock setDocumentSearchBean(DocumentSearchBean bean) {
        this.documentSearchBean = bean;
        return this;
    }

    public CollectionBeanMock setMemberService(MemberService service) {
        this.memberService = service;
        return this;
    }

    public CollectionBeanMock setCollectionService(CollectionService service) {
        this.collectionService = service;
        return this;
    }

    public CollectionBeanMock setFileService(FileService service) {
        this.fileService = service;
        return this;
    }

    public CollectionBeanMock setFileEntityService(FileEntityService service) {
        this.fileEntityService = service;
        return this;
    }

    public CollectionBeanMock setGlobalAdmissionContext(GlobalAdmissionContext context) {
        this.globalAdmissionContext = context;
        return this;
    }

    public CollectionBeanMock setACListService(ACListService acListService) {
        this.acListService = acListService;
        return this;
    }

    public CollectionBeanMock setTermVectorEntityService(TermVectorEntityService service) {
        this.termVectorEntityService = service;
        return this;
    }

}