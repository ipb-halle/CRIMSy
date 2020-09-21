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

import de.ipb_halle.lbac.search.document.StemmedWordGroup;
import de.ipb_halle.tx.text.ParseTool;
import de.ipb_halle.tx.text.TextRecord;
import de.ipb_halle.tx.text.properties.Language;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class SearchQueryStemmer {

    protected String filterDefinition = "queryParserFilterDefinition.json";

    public StemmedWordGroup stemmQuery(String queryString) {
        StemmedWordGroup back = new StemmedWordGroup();
        TextRecord tr = new TextRecord(queryString);
        int rank = 0;
        for (String lang : new String[]{"en", "de", "fr", "es", "pt"}) {
            tr.addProperty(new Language(0, queryString.length(), lang, rank));
            rank++;
        }
        ParseTool pt = new ParseTool();
        pt.setFilterDefinition(getClass().getResourceAsStream(filterDefinition));
        pt.initFilter();
        tr = pt.parseSingleTextRecord(tr);
        for (TextProperty prop : tr.getProperties(Word.TYPE)) {
            Word w = (Word) prop;
            String wStr = queryString.substring(w.getStart(), w.getEnd());
            if (wStr.trim().length() > 0) {
                back.addStemmedWord(wStr, w.getStemSet());
            }
        }
        return back;
    }
}
