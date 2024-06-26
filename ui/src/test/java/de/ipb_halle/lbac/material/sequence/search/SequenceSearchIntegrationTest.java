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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.bean.SearchMode;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchBean;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchMaskController;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchMaskValuesHolder;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchResultsTableController;
import de.ipb_halle.lbac.material.sequence.search.bean.SortItem;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParser;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParserException;
import de.ipb_halle.lbac.material.sequence.search.service.FastaRESTSearchService;
import de.ipb_halle.lbac.material.sequence.search.service.FastaRESTSearchServiceMock;
import de.ipb_halle.lbac.util.ResourceUtils;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ejb.EJBException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Test the data flow from the JSF backing bean layer down to the service layer
 * including database calls. The actual REST client call to the
 * fasta-search-service is mocked.
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
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
    private SequenceSearchMaskValuesHolder valuesHolder;
    private SequenceSearchResultsTableController tableController;
    private MessagePresenterMock messagePresenter;

    @BeforeEach
    public void init() throws FastaResultParserException {
        messagePresenter = getMessagePresenterMock();
        messagePresenter.resetMessages();

        Reader reader = ResourceUtils.readerForResourceFile("fastaresults/results7.txt");
        // This list is ordered by the E-value.
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
        valuesHolder = searchMaskController.getValuesHolder();
        tableController = searchBean.getResultsTableController();

        initParametersInControllers();
    }

    @Test
    public void test001_normalSearch1() {
        searchMaskController.actionStartMaterialSearch();

        assertNull(messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());

        /*
         * asserts on request
         */
        FastaSearchRequest request = requestRef.get();
        assertNull(request.getDatabaseConnectionString());
        // ToDo: asserts for request.getDatabaseQueries()

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
        assertTrue(sequence2.isEqualTo(results.get(1).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(2).getSequence()));
        assertTrue(sequence3.isEqualTo(results.get(3).getSequence()));
    }

    @Test
    public void test002_normalSearch2() {
        tableController.setLastUser(publicUser);
        tableController.setSortBy(SortItem.SUBJECTNAME);
        valuesHolder.setQuery("GCC");
        valuesHolder.setSearchMode(SearchMode.PROTEIN_DNA);
        valuesHolder.setTranslationTable(TranslationTable.STANDARD);
        valuesHolder.setMaxResults(10000);
        // ToDo: add filters for projects/material names/etc.
        searchMaskController.actionStartMaterialSearch();

        assertNull(messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());

        /*
         * asserts on request
         */
        FastaSearchRequest request = requestRef.get();
        assertNull(request.getDatabaseConnectionString());
        // ToDo: asserts for request.getDatabaseQueries()

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
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test004_fastaRESTSearchServiceThrowsProcessingException() {
        restServiceMock.setBehaviour(r -> {
            throw new ProcessingException("something else went wrong");
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test005_fastaRESTSearchServiceReturnsNotFound() {
        restServiceMock.setBehaviour(r -> {
            return Response.status(Response.Status.NOT_FOUND).entity("requested resource not found").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test006_fastaRESTSearchServiceReturnsBadRequest() {
        restServiceMock.setBehaviour(r -> {
            return Response.status(Response.Status.BAD_REQUEST).entity("you sent the wrong data").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test007_fastaRESTSearchServiceReturnsServerError() {
        restServiceMock.setBehaviour(r -> {
            return Response.serverError().entity("you sent the wrong data").build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test008_searchReturnsEmptyResultList() {
        restServiceMock.setBehaviour(r -> {
            FastaSearchResult result = new FastaSearchResult();
            return Response.ok(result).build();
        });
        searchMaskController.actionStartMaterialSearch();

        assertThat(tableController.getResults(), hasSize(0));
        assertEquals("sequenceSearch_noResults", messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    /*
     * That's unfair, because query cannot become null or empty due to bean
     * validation.
     */
    @Test
    public void test009_nullOrEmptyQuery_throwsEJBException() {
        valuesHolder.setQuery(null);
        EJBException exception = assertThrows(EJBException.class, () -> searchMaskController.actionStartMaterialSearch());
        assertThat(exception.getCause(), instanceOf(NullPointerException.class));

        valuesHolder.setQuery("");
        exception = assertThrows(EJBException.class, () -> searchMaskController.actionStartMaterialSearch());
        assertThat(exception.getCause(), instanceOf(NullPointerException.class));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SequenceSearchIntegrationTest.war");
        MaterialDeployment.add(deployment);
        MaterialBeanDeployment.add(deployment);
        UserBeanDeployment.add(deployment);
        ItemDeployment.add(deployment);
        PrintBeanDeployment.add(deployment);
        return deployment
                .deleteClass(FastaRESTSearchService.class)
                .addClass(FastaRESTSearchServiceMock.class)
                .addClass(SequenceSearchBean.class)
                .addClass(SendFileBeanMock.class)
                .addClass(MaterialOverviewBean.class);
    }

    private void initParametersInControllers() {
        tableController.setLastUser(publicUser);
        tableController.setSortBy(SortItem.IDENTITY);
        valuesHolder.setQuery("AAA");
        valuesHolder.setSearchMode(SearchMode.DNA_PROTEIN);
        valuesHolder.setTranslationTable(TranslationTable.EUPLOTID_NUCLEAR);
        valuesHolder.setMaxResults(42);
        // ToDo: add filters for projects/material names/etc.
    }
}
