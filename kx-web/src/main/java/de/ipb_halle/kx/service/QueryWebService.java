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

import de.ipb_halle.tx.text.ParseTool;
import de.ipb_halle.tx.text.TextRecord;
import de.ipb_halle.tx.text.properties.Language;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@WebServlet(urlPatterns = {"/query"})
public class QueryWebService extends HttpServlet {

    private final static long serialVersionUID = 1L;

    private final static String FILTER_DEFINITION = "queryParserFilterDefinition.json";

    private final Logger logger = LogManager.getLogger(QueryWebService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("doPost(): request received.");
        try {
            final PrintWriter out = resp.getWriter();
            out.write(processRequest(req));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private String processRequest(HttpServletRequest req) {
        StringBuilder query = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                query.append(line);
                query.append(" ");
            }
            return stemmQuery(query.toString());
        } catch (Exception e) {
            logger.warn(e);
        }
        return "Error";
    }

    public String stemmQuery(String queryString) {
        TextRecord textRecord = setupTextRecord(queryString);
        textRecord = setupParseTool().parseSingleTextRecord(textRecord);
        return getStemmedString(getSetOfWords(textRecord, queryString));
    }

    private String getStemmedString(Set<String> words) {
        StringBuilder sb = new StringBuilder();
        AtomicReference<String> sep = new AtomicReference<>("");
        for (String word : words) {
            sb.append(sep.getAndSet(" "));
            sb.append(word);
        }
        return sb.toString();
    }

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
