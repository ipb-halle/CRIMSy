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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipb_halle.lbac.entity.TermVector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class TermVectorParserTest {

    protected String TEST_ROOT = "target/test-classes/";
    private TermVectorParser instance = new TermVectorParser();

    @Test

    public void parseTermVectorJsonTest() throws IOException {

        String json = "{}";
        List<TermVector> termsVectors = instance.parseTermVectorJson(json, UUID.randomUUID());
        Assert.assertTrue(termsVectors.isEmpty());
        json = "{\"access\":{\"tf\":1},\"account\":{\"tf\":1}}";
        termsVectors = instance.parseTermVectorJson(json, UUID.randomUUID());
        Assert.assertEquals(2, termsVectors.size());

        //Realcase with a random text
        BufferedReader csvReader = new BufferedReader(new FileReader(TEST_ROOT + "termvectors/termvector.json"));
        String row;
        String text = null;
        while ((row = csvReader.readLine()) != null) {
            text = row;
        }
        csvReader.close();
        termsVectors = instance.parseTermVectorJson(text, UUID.randomUUID());
        Assert.assertEquals(4421, termsVectors.size());

        //Test with a broken json
        try {
            json = "{\"access\":{\"tf\":1},\"account\":{\"tf\":1}x}";
            termsVectors = instance.parseTermVectorJson(json, UUID.randomUUID());
        } catch (Exception e) {
            Assert.assertEquals(JsonParseException.class, e.getClass());
        }
    }

    @Test
    public void parseTermVectorToWordOriginsJsonTest() throws IOException {
        String xml = "";
        String originalText = "";
        List<StemmedWordOrigin> origins;// = instance.parseTermVectorXmlToWordOrigins(originalText, xml);
//        Assert.assertTrue(origins.isEmpty());

        Map<String, List<String>> targets = new HashMap<>();
        targets.put("a", Arrays.asList("a"));
        targets.put("can", Arrays.asList("Can"));
        targets.put("document", Arrays.asList("document"));
        targets.put("english", Arrays.asList("english"));
        targets.put("is", Arrays.asList("is", "i"));
        targets.put("languag", Arrays.asList("language"));
        targets.put("of", Arrays.asList("of"));
        targets.put("purpos", Arrays.asList("purpose"));
        targets.put("separation", Arrays.asList("separation"));
        targets.put("solr", Arrays.asList("solR"));
        targets.put("test", Arrays.asList("test"));
        targets.put("text", Arrays.asList("text"));
        targets.put("the", Arrays.asList("The"));
        targets.put("this", Arrays.asList("This"));
        targets.put("to", Arrays.asList("to"));
        targets.put("work", Arrays.asList("work"));

        de.ipb_halle.lbac.entity.Document doc=new de.ipb_halle.lbac.entity.Document();
        doc.setLanguage("de");
        xml = getRealWorldXmlString();

        origins = instance.parseTermVectorXmlToWordOrigins(doc, xml);
        Assert.assertEquals(origins.size(), targets.size());
        for (StemmedWordOrigin swo : origins) {
            Assert.assertNotNull(swo.getStemmedWord() + " not in target list", targets.get(swo.getStemmedWord()));

            List<String> targObjects = targets.get(swo.getStemmedWord());
            Assert.assertEquals("Original words of '" + swo.getStemmedWord() + "' does not match target size",
                    targObjects.size(),
                    swo.getOriginalWord().size());

            Assert.assertArrayEquals(
                    targObjects.toArray(new String[targObjects.size()]),
                    swo.getOriginalWord().toArray(new String[swo.getOriginalWord().size()])
            );
        }
    }

    private String getRealWorldXmlString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<response>\n"
                + "<lst name=\"responseHeader\"><int name=\"status\">0</int><int name=\"QTime\">1</int></lst><result name=\"response\" numFound=\"1\" start=\"0\"><doc><arr name=\"div\"><str>page</str></arr><arr name=\"storage_location\"><str>/</str></arr><str name=\"id\">4853c86a-c74a-465b-bc7d-103fe7d5b5f6</str><arr name=\"upload_date\"><str>Mon Oct 14 14:48:47 CEST 2019</str></arr><arr name=\"original_name\"><str>solr_example_de.pdf</str></arr><arr name=\"permission\"><str>PERMISSION DUMMY</str></arr><arr name=\"date\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"pdf_pdfversion\"><double>1.5</double></arr><arr name=\"xmp_creatortool\"><str>Microsoft® Word 2013</str></arr><arr name=\"stream_content_type\"><str>application/octet-stream</str></arr><arr name=\"access_permission_modify_annotations\"><bool>true</bool></arr><arr name=\"access_permission_can_print_degraded\"><bool>true</bool></arr><arr name=\"dc_creator\"><str>Mauz, Fabian</str></arr><arr name=\"dcterms_created\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"last_modified\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"dcterms_modified\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"dc_format\"><str>application/pdf; version=1.5</str></arr><arr name=\"last_save_date\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"pdf_docinfo_creator_tool\"><str>Microsoft® Word 2013</str></arr><arr name=\"access_permission_fill_in_form\"><bool>true</bool></arr><arr name=\"pdf_docinfo_modified\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"stream_name\"><str>solr_example_de.pdf</str></arr><arr name=\"meta_save_date\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"pdf_encrypted\"><bool>false</bool></arr><arr name=\"modified\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"content_type\"><str>application/pdf</str></arr><arr name=\"stream_size\"><long>194352</long></arr><arr name=\"pdf_docinfo_creator\"><str>Mauz, Fabian</str></arr><arr name=\"x_parsed_by\"><str>org.apache.tika.parser.DefaultParser</str><str>org.apache.tika.parser.pdf.PDFParser</str></arr><arr name=\"creator\"><str>Mauz, Fabian</str></arr><arr name=\"meta_author\"><str>Mauz, Fabian</str></arr><arr name=\"meta_creation_date\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"stream_source_info\"><str>solr_example_de.pdf</str></arr><arr name=\"created\"><str>Mon Oct 14 12:48:19 UTC 2019</str></arr><arr name=\"access_permission_extract_for_accessibility\"><bool>true</bool></arr><arr name=\"access_permission_assemble_document\"><bool>true</bool></arr><arr name=\"xmptpg_npages\"><long>1</long></arr><arr name=\"creation_date\"><date>2019-10-14T12:48:19Z</date></arr><arr name=\"access_permission_extract_content\"><bool>true</bool></arr><arr name=\"access_permission_can_print\"><bool>true</bool></arr><arr name=\"author\"><str>Mauz, Fabian</str></arr><arr name=\"producer\"><str>Microsoft® Word 2013</str></arr><arr name=\"access_permission_can_modify\"><bool>true</bool></arr><arr name=\"pdf_docinfo_producer\"><str>Microsoft® Word 2013</str></arr><arr name=\"pdf_docinfo_created\"><date>2019-10-14T12:48:19Z</date></arr><str name=\"language_s\">de</str><arr name=\"text_de\"><str> \n"
                + " \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + "  \n"
                + " \n"
                + "    \n"
                + " This is a text in english. The purpose of this document is to test the solR language separation! Can \n"
                + " \n"
                + " this work?  \n"
                + "  \n"
                + " \n"
                + "  </str></arr><arr name=\"language\"><str>de</str></arr><long name=\"_version_\">1647373009710743552</long></doc></result><lst name=\"termVectors\"><lst name=\"4853c86a-c74a-465b-bc7d-103fe7d5b5f6\"><str name=\"uniqueKey\">4853c86a-c74a-465b-bc7d-103fe7d5b5f6</str><lst name=\"text_de\"><lst name=\"a\"><lst name=\"offsets\"><int name=\"start\">143</int><int name=\"end\">144</int></lst></lst><lst name=\"can\"><lst name=\"offsets\"><int name=\"start\">232</int><int name=\"end\">235</int></lst></lst><lst name=\"document\"><lst name=\"offsets\"><int name=\"start\">182</int><int name=\"end\">190</int></lst></lst><lst name=\"english\"><lst name=\"offsets\"><int name=\"start\">153</int><int name=\"end\">160</int></lst></lst><lst name=\"is\"><lst name=\"offsets\"><int name=\"start\">140</int><int name=\"end\">142</int><int name=\"start\">191</int><int name=\"end\">192</int></lst></lst><lst name=\"languag\"><lst name=\"offsets\"><int name=\"start\">211</int><int name=\"end\">219</int></lst></lst><lst name=\"of\"><lst name=\"offsets\"><int name=\"start\">174</int><int name=\"end\">176</int></lst></lst><lst name=\"purpos\"><lst name=\"offsets\"><int name=\"start\">166</int><int name=\"end\">173</int></lst></lst><lst name=\"separation\"><lst name=\"offsets\"><int name=\"start\">220</int><int name=\"end\">230</int></lst></lst><lst name=\"solr\"><lst name=\"offsets\"><int name=\"start\">206</int><int name=\"end\">210</int></lst></lst><lst name=\"test\"><lst name=\"offsets\"><int name=\"start\">197</int><int name=\"end\">201</int></lst></lst><lst name=\"text\"><lst name=\"offsets\"><int name=\"start\">145</int><int name=\"end\">149</int></lst></lst><lst name=\"the\"><lst name=\"offsets\"><int name=\"start\">162</int><int name=\"end\">165</int><int name=\"start\">202</int><int name=\"end\">205</int></lst></lst><lst name=\"this\"><lst name=\"offsets\"><int name=\"start\">135</int><int name=\"end\">139</int><int name=\"start\">177</int><int name=\"end\">181</int><int name=\"start\">240</int><int name=\"end\">244</int></lst></lst><lst name=\"to\"><lst name=\"offsets\"><int name=\"start\">194</int><int name=\"end\">196</int></lst></lst><lst name=\"work\"><lst name=\"offsets\"><int name=\"start\">245</int><int name=\"end\">249</int></lst></lst></lst></lst></lst>\n"
                + "</response>";
    }

}
