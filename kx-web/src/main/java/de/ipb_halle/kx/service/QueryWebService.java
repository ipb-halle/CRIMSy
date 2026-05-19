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
package de.ipb_halle.kx.service;

import de.ipb_halle.kx.StemmingRequest;
import de.ipb_halle.kx.StemmingResponse;
import de.ipb_halle.tx.text.ParseTool;
import de.ipb_halle.tx.text.TextRecord;
import de.ipb_halle.tx.text.properties.Language;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Path("query")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryWebService {

    private final static long serialVersionUID = 1L;

    private final static String FILTER_DEFINITION = "queryParserFilterDefinition.json";

    private final Logger logger = LogManager.getLogger(QueryWebService.class);


    @POST
    public Response query(StemmingRequest request) {
        StemmingResponse response = stemmQuery(request);
        return Response.ok(response).build();
    }

    private StemmingResponse stemmQuery(StemmingRequest query) {
        TextRecord textRecord = setupTextRecord(query.getText());
        textRecord = setupParseTool().parseSingleTextRecord(textRecord);
        return createStemmingResponse(textRecord, query.getText());
    }

    private StemmingResponse createStemmingResponse(TextRecord textRecord, String queryString) {
        Set<String> stems = getSetOfWords(textRecord, queryString);
        return new StemmingResponse(stems.toArray(new String[0]));
    }
/*
    private String getStemmedString(Set<String> words) {
        StringBuilder sb = new StringBuilder();
        AtomicReference<String> sep = new AtomicReference<>("");
        for (String word : words) {
            sb.append(sep.getAndSet(" "));
            sb.append(word);
        }
        return sb.toString();
    }
*/
    private Set<String> getSetOfWords(TextRecord textRecord, String queryString) {
        Set<String> words = new HashSet<>();
        for (TextProperty prop : textRecord.getProperties(Word.TYPE)) {
            Word w = (Word) prop;
            String wStr = queryString.substring(w.getStart(), w.getEnd());
            if (wStr.trim().length() > 0) {
                for (String stem : w.getStemSet()) {
                    words.add(stem);
                }
            }
        }
        return words;
    }

    private TextRecord setupTextRecord(String queryString) {
        TextRecord textRecord = new TextRecord(queryString);
        int rank = 0;
        for (String lang : new String[]{"en", "de", "fr", "es", "pt"}) {
            textRecord.addProperty(new Language(0, queryString.length(), lang, rank));
            rank++;
        }
        return textRecord;
    }

    private ParseTool setupParseTool() {
        ParseTool parseTool = new ParseTool();
        parseTool.setFilterDefinition(getClass().getResourceAsStream(FILTER_DEFINITION));
        parseTool.initFilter();
        return parseTool;
    }
}
