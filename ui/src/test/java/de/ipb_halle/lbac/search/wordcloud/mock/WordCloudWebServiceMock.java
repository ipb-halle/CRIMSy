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
package de.ipb_halle.lbac.search.wordcloud.mock;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.TermFrequency;
import de.ipb_halle.lbac.file.TermFrequencyList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebRequest;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Path("rest/termvector")
public class WordCloudWebServiceMock {

    private final Logger LOGGER = LogManager.getLogger(WordCloudWebServiceMock.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public WordCloudWebRequest getTermVectorJSON(WordCloudWebRequest request) {
        LOGGER.info("Mocked WebServer entered");
        Node n = new Node();
        n.setBaseUrl("far away");
        n.setId(UUID.randomUUID());
        n.setInstitution("remote node");
        n.setLocal(false);
        n.setPublicNode(false);

        User u = new User();
        u.setId(10000);
        u.setName("remote User");
        u.setLogin("RU");
        u.setPassword("Should be obfuscated");
        u.setPhone("Should be obfuscated");
        u.setNode(n);

        Collection c = new Collection();
        c.setCountDocs(1L);
        c.setDescription("Collection from MockServer - Description");
        c.setId(100000);
        c.setIndexPath("/");
        c.setName("Mock-Collection");
        c.setNode(n);
        c.setOwner(u);
        c.setStoragePath("/");

        TermFrequencyList tfl = new TermFrequencyList();
        tfl.getTermFreq().add(new TermFrequency("word1", 2));
        tfl.getTermFreq().add(new TermFrequency("word2", 5));
        tfl.getTermFreq().add(new TermFrequency("word3", 3));
        tfl.getTermFreq().add(new TermFrequency("word4", 1));

        Document d = new Document();
        d.setCollectionId(1000);
        d.setCollection(c);
        d.setCollectionId(c.getId());
        d.setLanguage("de");
        d.setNode(n);
        d.setNodeId(n.getId());
        d.setOriginalName("remote Document");
        d.setTermFreqList(tfl);

        request.getDocumentsWithTerms().add(d);
        return request;
    }

}
