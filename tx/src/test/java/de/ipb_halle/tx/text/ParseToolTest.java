/*
 * Text eXtractor
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
package de.ipb_halle.tx.text;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fbroda
 */
public class ParseToolTest {

    private final static String filterDefinition = "ParseToolTestFilterDefinition.json";
    private final static String document001 = "/testDocuments/wordDetector.pdf";

    @Test
    public void test001_parsePDF() throws Exception {
        ParseTool parser = new ParseTool();
        parser.setFilterDefinition(this.getClass().getResourceAsStream(filterDefinition));
        parser.setInputStream(this.getClass().getResourceAsStream(document001));
        parser.initFilter();
        parser.parse();
        int wordCount = (Integer) parser.getFilterData().getValue(WordDetectorFilter.WORD_COUNT);

        Assert.assertEquals("message", 14, wordCount);
    }
}