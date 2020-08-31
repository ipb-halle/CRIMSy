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
package de.ipb_halle.lbac.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Parses a termvector json represantation from the solr instance into a list of
 * jpa objects.
 *
 * @author fmauz
 */
public class TermVectorParser {

    public List<TermVector> parseTermVectorJson(
            String json,
            Integer documentId) throws IOException {

        List<TermVector> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tv = mapper.readTree(json);
        Iterator<Entry<String, JsonNode>> iter = tv.fields();

        while (iter.hasNext()) {

            Entry<String, JsonNode> n = iter.next();

            int tf = n.getValue().get("tf").asInt();
            result.add(new TermVector(n.getKey(), documentId, tf));
        }

        return result;
    }

    public List<StemmedWordOrigin> parseTermVectorXmlToWordOrigins(
            de.ipb_halle.lbac.entity.Document d,
            String xml)
            throws IOException {
        List<StemmedWordOrigin> results = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            String originalText = getOriginalText("text_" + d.getLanguage(), document);
            NodeList nl = document.getElementsByTagName("lst");
            Node n = getNodeWithAttribute("termVectors", nl);

            NodeList terms = n.getChildNodes().item(0).getChildNodes().item(1).getChildNodes();
            for (int i = 0; i < terms.getLength(); i++) {
                Node termNode = terms.item(i);
                String stemmedWord = termNode.getAttributes().item(0).getNodeValue();
                StemmedWordOrigin wordOrigin = new StemmedWordOrigin();
                results.add(wordOrigin);
                wordOrigin.setStemmedWord(stemmedWord);
                NodeList offSetNL = termNode.getChildNodes().item(0).getChildNodes();
                int startPosition = -1;
                for (int j = 0; j < offSetNL.getLength(); j++) {

                    if (j % 2 == 0) {

                        startPosition = Integer.parseInt(offSetNL.item(j).getTextContent());
                    } else {
                        int endposition = Integer.parseInt(offSetNL.item(j).getTextContent());
                        wordOrigin.addOriginWord(originalText.substring(startPosition, endposition));
                    }
                }
            }

            int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;

    }

    private Node getNodeWithAttribute(String attr, NodeList nl) {
        for (int i = 0; i < nl.getLength(); i++) {
            NamedNodeMap nodeAttributes = nl.item(i).getAttributes();
            for (int j = 0; j < nodeAttributes.getLength(); j++) {
                if (nodeAttributes.item(j).getNodeValue().equals(attr)) {
                    return nl.item(i);
                }
            }
        }
        return null;
    }

    private String getOriginalText(String textField, Document xml) {
        NodeList arrNl = xml.getElementsByTagName("arr");
        for (int i = 0; i < arrNl.getLength(); i++) {
            NamedNodeMap attrMap = arrNl.item(i).getAttributes();
            Node n = attrMap.getNamedItem("name");
            if (n != null && n.getTextContent().equals(textField)) {
                return arrNl.item(i).getChildNodes().item(0).getTextContent();

            }
        }
        return null;
    }
}
