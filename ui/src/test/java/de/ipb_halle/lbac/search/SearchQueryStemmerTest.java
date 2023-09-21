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
import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SearchQueryStemmerTest {


    @Test
    public void test001_stemmQuery() {
        SearchQueryStemmer sqs=new SearchQueryStemmer();
        Set<String> results=sqs.stemmQuery("Werkzeuge gebrauchen");
        Assert.assertTrue(results.contains("werkzeug"));
        Assert.assertTrue(results.contains("gebrauch"));
        Assert.assertEquals(sqs.stemmQuery("").size(), 0);
    }

}
