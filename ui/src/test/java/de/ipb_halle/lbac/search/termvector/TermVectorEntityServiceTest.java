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
package de.ipb_halle.lbac.search.termvector;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.file.TermVector;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.StemmedWordOrigin;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *
 * @author fmauz
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Arquillian.class)
public class TermVectorEntityServiceTest extends TestBase {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private ACListService aclistService;

    private Random random = new Random();

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TermVectorEntityServiceTest.war")
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(FileObject.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class);
    }

    @Test
    public void test001_termVectorEntityService() {

        User u = createUser(
                "testuser",
                "testuser");

        termVectorEntityService.deleteTermVectors();
        ACList acl = new ACList();
        acl.setName("test");
        acl.addACE(u, ACPermission.values());
        aclistService.save(acl);
        Collection col1 = createCollection("collection1", acl, u);
        Collection col2 = createCollection("collection2", acl, u);
        col1 = collectionService.save(col1);
        col2 = collectionService.save(col2);

        FileObject fE1 = createFileObject(col1, "en", "file1", u);
        FileObject fE2 = createFileObject(col1, "en", "file2", u);
        FileObject fE3 = createFileObject(col1, "de", "file3", u);
        FileObject fE4 = createFileObject(col2, "en", "file4", u);

        fE1 = fileEntityService.save(fE1);
        fE2 = fileEntityService.save(fE2);
        fE3 = fileEntityService.save(fE3);
        fE4 = fileEntityService.save(fE4);

        List<TermVector> vectors = new ArrayList<>();

        vectors.add(new TermVector("testStemWord", fE1.getId(), 3));
        vectors.add(new TermVector("testStemWord2", fE1.getId(), 4));
        vectors.add(new TermVector("testStemWord", fE2.getId(), 5));
        vectors.add(new TermVector("testStemWord3", fE3.getId(), 7));
        vectors.add(new TermVector("testStemWord", fE4.getId(), 11));

        fileEntityService.saveTermVectors(vectors);

        List<Integer> ids = new ArrayList<>();
        Map<String, Integer> results = termVectorEntityService.getTermVector(ids, 10);
        Assert.assertTrue(results.isEmpty());

        ids.add(fE1.getId());
        results = termVectorEntityService.getTermVector(ids, 10);
        Assert.assertEquals(2, results.keySet().size());
        int sum = 0;
        sum = results.values().stream().map((i) -> i).reduce(sum, Integer::sum);
        Assert.assertEquals(7, sum);

        ids.add(fE2.getId());
        ids.add(fE3.getId());
        ids.add(fE4.getId());
        results = termVectorEntityService.getTermVector(ids, 10);
        Assert.assertEquals(3, results.keySet().size());
        sum = 0;
        sum = results.values().stream().map((i) -> i).reduce(sum, Integer::sum);
        Assert.assertEquals(30, sum);

        results = termVectorEntityService.getTermVector(ids, 2);
        Assert.assertEquals(2, results.keySet().size());
        int mostFrequentWord = results.get("testStemWord");
        Assert.assertEquals(19, mostFrequentWord);
        int secondMostFrequentWord = results.get("testStemWord3");
        Assert.assertEquals(7, secondMostFrequentWord);

        int totalSum = termVectorEntityService.getSumOfAllWordsFromAllDocs();
        Assert.assertTrue(totalSum >= 30);

        termVectorEntityService.deleteTermVectorOfCollection(col2);
        totalSum = termVectorEntityService.getSumOfAllWordsFromAllDocs();
        Assert.assertEquals(19, totalSum);

        StemmedWordOrigin wordOrigin = new StemmedWordOrigin();
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList("origalWord1", "origalWord2", "origalWord3"));
        wordOrigin.setOriginalWord(set);
        wordOrigin.setStemmedWord("testStemWord");

        termVectorEntityService.saveUnstemmedWordsOfDocument(Arrays.asList(wordOrigin), fE1.getId());

        List< StemmedWordOrigin> words = termVectorEntityService.loadUnstemmedWordsOfDocument(fE1.getId(), "testStemWord");
        Assert.assertEquals("Loading of unstemmed words not correct", 1, words.size());
        Assert.assertEquals(3, words.get(0).getOriginalWord().size());

    }

    private Collection createCollection(String name, ACList acl, User owner) {
        Collection col = new Collection();
        col.setNode(this.nodeService.getLocalNode());
        col.setName(name);
        col.setDescription(name);
        col.setIndexPath("/doc/test");
        col.setACList(acl);
        col.setOwner(owner);
        return col;
    }

    private FileObject createFileObject(Collection col, String language, String fileName, User owner) {
        FileObject fO = new FileObject();
        fO.setCollection(col);
        fO.setCreated(new Date());
        fO.setDocument_language(language);
        fO.setFileLocation(fileName);
        fO.setHash(fileName);
        fO.setName(fileName);
        fO.setUser(owner);
        return fO;
    }
}
