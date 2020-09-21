/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.search.mocks;

import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.NodeService;

/**
 *
 * @author fmauz
 */
public class DocumentSearchBeanMock extends DocumentSearchBean {

    public DocumentSearchBeanMock setDocumentSearchService(DocumentSearchService service) {
        this.documentSearchService = service;
        return this;
    }

    public DocumentSearchBeanMock setCollectionBean(CollectionBean bean) {
        this.collectionBean = bean;
        return this;
    }

    public DocumentSearchBeanMock setFileEntityService(FileEntityService service) {
        this.fileEntityService = service;
        return this;

    }

    public DocumentSearchBeanMock setCollectionService(CollectionService service) {
        this.collectionService = service;
        return this;
    }

    public DocumentSearchBeanMock setDocumentSearchOrchestrator(DocumentSearchOrchestrator or) {
        this.orchestrator = or;
        return this;
    }

    public DocumentSearchBeanMock setNodeService(NodeService service) {
        this.nodeService = service;
        return this;
    }

}
