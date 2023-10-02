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
package de.ipb_halle.lbac.base;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.service.NodeService;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.openejb.loader.Files;

/**
 *
 * @author fmauz
 */
public class DocumentCreator {

    protected Collection collection;
    protected AsyncContextMock asynContext;
    protected String exampleDocsRootFolder = "target/test-classes/exampledocs/";

    protected FileObjectService fileObjectService;
    protected CollectionService collectionService;
    protected NodeService nodeService;
    protected TermVectorService termVectorService;
    protected User user;

    private Map<String, List<TermVector>> termVectorMap;
    private Map<String, List<StemmedWordOrigin>>  stemmedWordsMap;

    public DocumentCreator(
            FileObjectService fileObjectService,
            CollectionService collectionService,
            NodeService nodeService,
            TermVectorService termVectorService) {

        this.fileObjectService = fileObjectService;
        this.collectionService = collectionService;
        this.nodeService = nodeService;
        this.termVectorService = termVectorService;
        setupTermVectorMap();
        setupStemmedWordsMap();
    }

    public Collection uploadDocuments(User user, String collectionName, String... files) throws FileNotFoundException, InterruptedException {
        Files.delete(Paths.get("target/test-classes/collections").toFile());
        this.user = user;
        createAndSaveNewCol(collectionName);
        for (String file : files) {
            setupDocument(file);
        }
        return collection;
    }

    private void createAndSaveNewCol(String colName) {
        collection = new Collection();
        collection.setACList(GlobalAdmissionContext.getPublicReadACL());
        collection.setDescription("xxx");
        collection.setIndexPath("/");
        collection.setName(colName);
        collection.setNode(nodeService.getLocalNode());
        collection.setOwner(user);
        collection.setStoragePath("/");
        collection = collectionService.save(collection);
        collection.COLLECTIONS_BASE_FOLDER = "target/test-classes/collections";
    }

    private void setupDocument(String name) {
        FileObject fileObj = new FileObject();
        fileObj.setName(name);
        fileObj.setCollectionId(collection.getId());
        fileObj.setFileLocation("dummy/location/" + name);
        fileObj = fileObjectService.save(fileObj);

        saveTermVectors(name, fileObj.getId());
        termVectorService.saveUnstemmedWordsOfDocument(stemmedWordsMap.get(name), fileObj.getId());
    }

    private void saveTermVectors(String name, Integer fileId) {
        List<TermVector> tvl = termVectorMap.get(name);
        for (TermVector tv : tvl) {
            tv.setFileId(fileId);
        }
        termVectorService.saveTermVectors(tvl);
    }
    
    private void setupTermVectorMap() {
        termVectorMap = new HashMap<> ();
        termVectorMap.put("Document1.pdf", Arrays.asList(
                // root, file, freq
                new TermVector("java", 0, 3),
                new TermVector("failure", 0, 37)));

        termVectorMap.put("Document2.pdf", Arrays.asList(
                new TermVector("java", 0, 2),
                new TermVector("failure", 0, 98)));

        termVectorMap.put("Document3.pdf", Arrays.asList(
                new TermVector("failure", 0, 25)));

        termVectorMap.put("Document18.pdf", Arrays.asList(
                new TermVector("check", 0, 1),
                new TermVector("java", 0, 1),
                new TermVector("stem", 0, 1),
                new TermVector("tini", 0, 1),
                new TermVector("word", 0, 1)));

        termVectorMap.put("Wasserstoff.docx", Arrays.asList(
                new TermVector("dokumentsuch", 0, 1),
                new TermVector("vorschritt", 0, 1),
                new TermVector("gefund", 0, 1),
                new TermVector("material", 0, 1),
                new TermVector("durchzufuhr", 0, 1),
                new TermVector("wasserstoff", 0, 1),
                new TermVector("testdokument", 0, 1)));
    }

    private void setupStemmedWordsMap() {
        stemmedWordsMap = new HashMap<> ();

        stemmedWordsMap.put("Document1.pdf", Arrays.asList(
                new StemmedWordOrigin("java", "java"),
                new StemmedWordOrigin("failure", "failure")));

        stemmedWordsMap.put("Document2.pdf", Arrays.asList(
                new StemmedWordOrigin("java", "java"),
                new StemmedWordOrigin("failure", "failure")));

        stemmedWordsMap.put("Document3.pdf", Arrays.asList(
                new StemmedWordOrigin("failure", "failure")));

        stemmedWordsMap.put("Document18.pdf", Arrays.asList(
                new StemmedWordOrigin("check", "check"),
                new StemmedWordOrigin("java", "java"),
                new StemmedWordOrigin("stem", "stemming"),
                new StemmedWordOrigin("tini", "tiny"),
                new StemmedWordOrigin("word", "word")));

        stemmedWordsMap.put("Wasserstoff.docx", Arrays.asList(
                new StemmedWordOrigin("dokumentsuch", "dokumentsuche"),
                new StemmedWordOrigin("vorschritt", "vorschritt"),
                new StemmedWordOrigin("gefund", "gefunden"),
                new StemmedWordOrigin("material", "material"),
                new StemmedWordOrigin("durchzufuhr", "durchzuführen"),
                new StemmedWordOrigin("wasserstoff", "wasserstoff"),
                new StemmedWordOrigin("testdokument", "testdokument")));
    }
}
