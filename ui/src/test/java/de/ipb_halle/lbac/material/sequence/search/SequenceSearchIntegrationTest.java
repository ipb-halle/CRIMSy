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
package de.ipb_halle.lbac.material.sequence.search;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchQuery;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.fasta_search_service.models.search.TranslationTable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.FastaRESTSearchService;
import de.ipb_halle.lbac.material.sequence.FastaRESTSearchServiceMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.bean.SearchMode;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchBean;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchMaskController;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchResultsTableController;
import de.ipb_halle.lbac.material.sequence.search.bean.SortItem;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParser;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParserException;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import org.junit.Test;

/**
 * Test the data flow from the JSF backing bean layer down to the service layer
 * including database calls. The actual REST client call to the
 * fasta-search-service is mocked.
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class SequenceSearchIntegrationTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private SequenceSearchBean searchBean;

    @Inject
    private MaterialService materialService;

    @Inject
    private FastaRESTSearchServiceMock restServiceMock;

    private Sequence sequence1, sequence2, sequence3;
    private FastaResult fastaResult1, fastaResult2, fastaResult3a, fastaResult3b;
    private List<FastaResult> fastaResults;
    private AtomicReference<FastaSearchRequest> requestRef;
    private SequenceSearchMaskController searchMaskController;
    private SequenceSearchResultsTableController tableController;

    @Before
    public void init() throws FastaResultParserException {
        Reader reader = readerForResourceFile("fastaresults/results1.txt");
        List<FastaResult> parserResults = new FastaResultParser(reader).parse();

        SequenceData data1 = SequenceData.builder().sequenceString("seq1").sequenceType(SequenceType.DNA).build();
        SequenceData data2 = SequenceData.builder().sequenceString("seq2").sequenceType(SequenceType.PROTEIN).build();
        SequenceData data3 = SequenceData.builder().sequenceString("seq3").sequenceType(SequenceType.DNA).build();

        List<MaterialName> names1 = Arrays.asList(new MaterialName("firstName1", "en", 1),
                new MaterialName("secondName1", "de", 100));
        List<MaterialName> names2 = Arrays.asList(new MaterialName("firstName2", "en", 1),
                new MaterialName("secondName2", "de", 100));
        List<MaterialName> names3 = Arrays.asList(new MaterialName("firstName3", "en", 1),
                new MaterialName("secondName3", "de", 100));

        sequence1 = new Sequence(names1, null, data1);
        sequence2 = new Sequence(names2, null, data2);
        sequence3 = new Sequence(names3, null, data3);

        materialService.saveMaterialToDB(sequence1, context.getAdminOnlyACL().getId(), new HashMap<>(), publicUser);
        materialService.saveMaterialToDB(sequence2, context.getAdminOnlyACL().getId(), new HashMap<>(), publicUser);
        materialService.saveMaterialToDB(sequence3, context.getAdminOnlyACL().getId(), new HashMap<>(), publicUser);

        fastaResult1 = parserResults.get(0);
        fastaResult2 = parserResults.get(1);
        fastaResult3a = parserResults.get(2);
        fastaResult3b = parserResults.get(3);

        fastaResult1.setSubjectSequenceName(Integer.toString(sequence1.getId()));
        fastaResult2.setSubjectSequenceName(Integer.toString(sequence2.getId()));
        fastaResult3a.setSubjectSequenceName(Integer.toString(sequence3.getId()));
        fastaResult3b.setSubjectSequenceName(Integer.toString(sequence3.getId()));

        fastaResults = Arrays.asList(fastaResult3a, fastaResult1, fastaResult2, fastaResult3b);

        /*
         * Need a wrapper object to set a local variable inside a lambda. See
         * https://stackoverflow.com/q/30026824
         */
        // Use requestRef.get() after executing the search.
        requestRef = new AtomicReference<>();
        restServiceMock.setBehaviour(r -> {
            requestRef.set(r);

            FastaSearchResult result = new FastaSearchResult();
            result.setProgramOutput("abc");
            result.setResults(fastaResults);

            return Response.ok(result).build();
        });

        searchMaskController = searchBean.getSearchMaskController();
        tableController = searchBean.getResultsTableController();
    }

    @Test
    public void test001_normalSearch1() {
        initParametersInControllers();
        searchMaskController.actionStartMaterialSearch();

        /*
         * asserts on request
         */
        FastaSearchRequest request = requestRef.get();
        assertNull(request.getDatabaseConnectionString());
        // TODO: asserts for request.getDatabaseQueries()

        FastaSearchQuery query = request.getSearchQuery();
        assertEquals("AAA", query.getQuerySequence());
        assertEquals("DNA", query.getQuerySequenceType());
        assertEquals("PROTEIN", query.getLibrarySequenceType());
        assertEquals(10, query.getTranslationTable());
        assertEquals(42, query.getMaxResults());

        /*
         * asserts on results
         */
        List<FastaResultDisplayWrapper> results = tableController.getResults();
        assertThat(results, hasSize(4));
        assertTrue(sequence1.isEqualTo(results.get(0).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(1).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(2).getSequence()));
        assertTrue(sequence2.isEqualTo(results.get(3).getSequence()));
    }

    @Test
    public void test002_normalSearch2() {
        tableController.setLastUser(publicUser);
        tableController.setSortBy(SortItem.SUBJECTNAME);
        searchMaskController.setQuery("GCC");
        searchMaskController.setSearchMode(SearchMode.PROTEIN_DNA);
        searchMaskController.setTranslationTable(TranslationTable.STANDARD);
        searchMaskController.setMaxResults(10000);
        // TODO: add filters for projects/material names/etc.
        searchMaskController.actionStartMaterialSearch();

        /*
         * asserts on request
         */
        FastaSearchRequest request = requestRef.get();
        assertNull(request.getDatabaseConnectionString());
        // TODO: asserts for request.getDatabaseQueries()

        FastaSearchQuery query = request.getSearchQuery();
        assertEquals("GCC", query.getQuerySequence());
        assertEquals("PROTEIN", query.getQuerySequenceType());
        assertEquals("DNA", query.getLibrarySequenceType());
        assertEquals(1, query.getTranslationTable());
        assertEquals(10000, query.getMaxResults());

        /*
         * asserts on results
         */
        List<FastaResultDisplayWrapper> results = tableController.getResults();
        assertThat(results, hasSize(4));
        assertTrue(sequence1.isEqualTo(results.get(0).getSequence()));
        assertTrue(sequence2.isEqualTo(results.get(1).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(2).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(3).getSequence()));
    }

    @Test
    public void test003_fastaRESTSearchServiceThrowsResponseProcessingException() {
        restServiceMock.setBehaviour(r -> {
            throw new ResponseProcessingException(Response.ok().build(), "something went wrong");
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        // TODO: assert on errors on the level of tableController
    }

    @Test
    public void test004_fastaRESTSearchServiceThrowsProcessingException() {
        restServiceMock.setBehaviour(r -> {
            throw new ProcessingException("something else went wrong");
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        // TODO: assert on errors on the level of tableController
    }

    @Test
    public void test005_fastaRESTSearchServiceReturnsNotFound() {
        restServiceMock.setBehaviour(r -> {
            return Response.status(Response.Status.NOT_FOUND).entity("requested resource not found").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        // TODO: assert on errors on the level of tableController
    }

    @Test
    public void test006_fastaRESTSearchServiceReturnsBadRequest() {
        restServiceMock.setBehaviour(r -> {
            return Response.status(Response.Status.BAD_REQUEST).entity("you sent the wrong data").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        // TODO: assert on errors on the level of tableController
    }

    @Test
    public void test007_fastaRESTSearchServiceReturnsServerError() {
        restServiceMock.setBehaviour(r -> {
            return Response.serverError().entity("you sent the wrong data").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        // TODO: assert on errors on the level of tableController
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SequenceSearchBeanTest.war");
        MaterialDeployment.add(deployment);
        UserBeanDeployment.add(deployment);
        return deployment.deleteClass(FastaRESTSearchService.class).addClass(FastaRESTSearchServiceMock.class)
                .addClass(SequenceSearchBean.class).addClass(SendFileBeanMock.class);
    }

    private void initParametersInControllers() {
        tableController.setLastUser(publicUser);
        tableController.setSortBy(SortItem.IDENTITY);
        searchMaskController.setQuery("AAA");
        searchMaskController.setSearchMode(SearchMode.DNA_PROTEIN);
        searchMaskController.setTranslationTable(TranslationTable.EUPLOTID_NUCLEAR);
        searchMaskController.setMaxResults(42);
        // TODO: add filters for projects/material names/etc.
    }

    private Reader readerForResourceFile(String filename) {
        return new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename));
    }
}