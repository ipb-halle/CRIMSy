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
package de.ipb_halle.lbac.search;

/**
 *
 * @author fmauz
 */
import de.ipb_halle.lbac.search.document.*;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.file.FileSearchRequest;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

public class SearchQueryStemmerTest {


    @Test
    public void test001_stemmQuery() throws FileNotFoundException {
        SearchQueryStemmer sqs=new SearchQueryStemmer();
        Map<String,Set<String>> results=sqs.stemmQuery("Werkzeuge gebrauchen");
        Assert.assertTrue(results.get("Werkzeuge").contains("werkzeug"));
        Assert.assertTrue(results.get("gebrauchen").contains("gebrauch"));
        Assert.assertEquals(sqs.stemmQuery("").size(), 0);
        results=sqs.stemmQuery("*");
        results=sqs.stemmQuery("    ");
        results=sqs.stemmQuery("\n \r");
        int i=0;
    }

}
