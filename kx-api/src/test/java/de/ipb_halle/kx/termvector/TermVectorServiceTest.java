/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.termvector;

import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@TestMethodOrder(MethodName.class)
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class TermVectorServiceTest {

    @Inject
    private EntityManagerService ems;

    @Inject
    private TermVectorService termVectorService;

    @Inject
    private FileObjectService fileObjectService;


    private Random random = new Random();

    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "TermVectorServiceTest.war")
                .addClass(FileObjectService.class)
                .addClass(FileObject.class)
                .addClass(TermVectorService.class)
                .addClass(TermVector.class)
                .addClass(EntityManagerService.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @AfterEach
    public void cleanUp() {

//      entityManagerService.getEntityManager()
//              .createNativeQuery(String.format("DELETE FROM usersgroups WHERE name='%s'", user.getName()))
//              .executeUpdate();
//      entityManagerService.getEntityManager()
//              .createNativeQuery("DELETE FROM files")
//              .executeUpdate();

    }

    @Test
    public void test001_termVectorService() {

        termVectorService.deleteTermVectors();

        FileObject fE1 = createFileObject("en", "file1");
        FileObject fE2 = createFileObject("en", "file2");
        FileObject fE3 = createFileObject("de", "file3");
        FileObject fE4 = createFileObject("en", "file4");

        fE1 = fileObjectService.save(fE1);
        fE2 = fileObjectService.save(fE2);
        fE3 = fileObjectService.save(fE3);
        fE4 = fileObjectService.save(fE4);

        List<TermVector> vectors = new ArrayList<>();

        vectors.add(new TermVector("testStemWord", fE1.getId(), 3));
        vectors.add(new TermVector("testStemWord2", fE1.getId(), 4));
        vectors.add(new TermVector("testStemWord", fE2.getId(), 5));
        vectors.add(new TermVector("testStemWord3", fE3.getId(), 7));
        vectors.add(new TermVector("testStemWord", fE4.getId(), 11));

        termVectorService.saveTermVectors(vectors);

        List<Integer> ids = new ArrayList<>();
        List<TermFrequency> results = termVectorService.getTermVector(ids, 10);
        Assert.assertTrue("Result list for empty input is empty", results.isEmpty());

        ids.add(fE1.getId());
        results = termVectorService.getTermVector(ids, 10);
        Assert.assertEquals(2, results.size());
        int sum = 0;
        sum = results.stream().map((i) -> i.getFrequency()).reduce(sum, Integer::sum);
        Assert.assertEquals(7, sum);

        ids.add(fE2.getId());
        ids.add(fE3.getId());
        ids.add(fE4.getId());
        results = termVectorService.getTermVector(ids, 10);
        Assert.assertEquals(3, results.size());
        sum = 0;
        sum = results.stream().map((i) -> i.getFrequency()).reduce(sum, Integer::sum);
        Assert.assertEquals(30, sum);

        results = termVectorService.getTermVector(ids, 2);
        Assert.assertEquals(2, results.size());

        Assert.assertEquals("Frequency of most frequent word", 19l, results.get(0).getFrequency().longValue());
        Assert.assertEquals("Term of most frequent word", "testStemWord", results.get(0).getTerm());
        Assert.assertEquals("Frequency of 2nd most frequent word", 7l, results.get(1).getFrequency().longValue());
        Assert.assertEquals("Term of 2nd most frequent word", "testStemWord3", results.get(1).getTerm());

        int totalSum = termVectorService.getSumOfAllWordsFromAllDocs();
        Assert.assertTrue("Total number of words is >= 30", totalSum >= 30);

        termVectorService.deleteTermVector(fE4);
        totalSum = termVectorService.getSumOfAllWordsFromAllDocs();
        Assert.assertEquals("Total number of words after deletion = 19", 19, totalSum);

        List<StemmedWordOrigin> wordOriginList = new ArrayList<> ();
        wordOriginList.add(new StemmedWordOrigin("testStemWord", "originalWord1"));
        wordOriginList.add(new StemmedWordOrigin("testStemWord", "originalWord2"));
        wordOriginList.add(new StemmedWordOrigin("testStemWord", "originalWord3"));

        termVectorService.saveUnstemmedWordsOfDocument(wordOriginList, fE1.getId());

        List<StemmedWordOrigin> words = termVectorService.loadUnstemmedWordsOfDocument(fE1.getId(), "testStemWord");
        Assert.assertEquals("Loading of unstemmed words not correct", 3, words.size());
        Assert.assertTrue(words.get(0).getStemmedWord().equals("testStemWord"));
        Assert.assertTrue(words.get(0).getOriginalWord().startsWith("originalWord"));

    }

    private FileObject createFileObject(String language, String fileName) {
        FileObject fO = new FileObject();
        fO.setCollectionId(null);
        fO.setCreated(new Date());
        fO.setDocumentLanguage(language);
        fO.setFileLocation(fileName);
        fO.setHash(fileName);
        fO.setName(fileName);
        fO.setUserId(null);
        return fO;
    }
}
