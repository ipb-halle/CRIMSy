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

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fmauz
 */
public class SolrSearcherTest {

    private SolrSearcher instance = new SolrSearcher();

    @Test
    public void convertQueryToLanguagesTest() {

        String expection = "(text_de:hallo )(text_en:hallo )(text_es:hallo )(text_pt:hallo )(text_gl:hallo )(text_fr:hallo )";
        String result = instance.convertQueryToLanguages("hallo");
        Assert.assertEquals(expection, result);

        expection = "(text_de:hallo text_de:test )"
                + "(text_en:hallo text_en:test )"
                + "(text_es:hallo text_es:test )"
                + "(text_pt:hallo text_pt:test )"
                + "(text_gl:hallo text_gl:test )"
                + "(text_fr:hallo text_fr:test )";

        result = instance.convertQueryToLanguages("hallo test");

        Assert.assertEquals(expection, result);

        expection = "(text_de:hallo AND text_de:test )"
                + "(text_en:hallo AND text_en:test )"
                + "(text_es:hallo AND text_es:test )"
                + "(text_pt:hallo AND text_pt:test )"
                + "(text_gl:hallo AND text_gl:test )"
                + "(text_fr:hallo AND text_fr:test )";

        result = instance.convertQueryToLanguages("hallo AND test");

        Assert.assertEquals(expection, result);
    }

//    @Test
//    public void xx() throws Exception {
//        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/javabooks").build();
//        SolrQuery query = new SolrQuery();
//
//        query.setParam("q", "*:*");
//
//        QueryResponse response = solr.query(query);
//        SolrDocumentList list = response.getResults();
//        String text = null;
//        if (list != null && !list.isEmpty()) {
//            List<String> o = (List<String>) list.get(0).getFieldValue("text_de");
//
//            text = o.get(0);//.replace("\n", "").replace("  ", " ").replace(".", "").replace("?", "").replace("!", "").trim().toLowerCase();
//        }
//
//        Client client = ClientBuilder.newClient();
//        String termVectorResponse = client.target("http://localhost:8983/solr/javabooks")
//                .path("tvrh")
//                .queryParam("q", "id:" + "4853c86a-c74a-465b-bc7d-103fe7d5b5f6")
//                .queryParam("tv.offsets", true)
//                .queryParam("tv.fl", "text_de")
//                .queryParam("wt", "xml")
//                .queryParam("indent", "off")
//                .queryParam("json.nl", "map")
//                .request(MediaType.TEXT_XML)
//                .get(String.class);
//        //*** select subTree "termVectors"->"<docId>"->"_general_" ***
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder;
//        List<StemmedWordOrigin> results = new ArrayList<>();
//        try {
//            builder = factory.newDocumentBuilder();
//            Document document = builder.parse(new InputSource(new StringReader(termVectorResponse)));
//            NodeList nl = document.getElementsByTagName("lst");
//            Node n = getNodeWithAttribute("termVectors", nl);
//
//            NodeList terms = n.getChildNodes().item(0).getChildNodes().item(1).getChildNodes();
//            for (int i = 0; i < terms.getLength(); i++) {
//                Node termNode = terms.item(i);
//                String stemmedWord = termNode.getAttributes().item(0).getNodeValue();
//                StemmedWordOrigin wordOrigin = new StemmedWordOrigin();
//                results.add(wordOrigin);
//                wordOrigin.setStemmedWord(stemmedWord);
//                NodeList offSetNL = termNode.getChildNodes().item(0).getChildNodes();
//                int startPosition = -1;
//                for (int j = 0; j < offSetNL.getLength(); j++) {
//                    if (j % 2 == 0) {
//                        startPosition = Integer.parseInt(offSetNL.item(j).getTextContent());
//                    } else {
//                        int endposition = Integer.parseInt(offSetNL.item(j).getTextContent());
//                        wordOrigin.getOriginalWord().add(text.substring(startPosition, endposition));
//                    }
//                }
//            }
//
//            int i = 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        int i = 0;
//    }

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
}
