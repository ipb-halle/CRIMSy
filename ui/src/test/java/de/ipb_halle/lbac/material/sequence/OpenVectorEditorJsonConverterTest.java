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
package de.ipb_halle.lbac.material.sequence;

import static de.ipb_halle.lbac.base.JsonAssert.assertJsonEquals;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * 
 * @author flange
 */
public class OpenVectorEditorJsonConverterTest {
    private OpenVectorEditorJsonConverter converter = new OpenVectorEditorJsonConverter();
    private String json;
    private String convertedJson;
    private SequenceData data;
//    private List<SequenceAnnotation> annotations;
//    private SequenceAnnotation annotation;
    private String annotations;

    @Test
    public void test001a_jsonToSequenceWithDNASequenceLinearWithoutFeatures()
            throws Exception {
        json = readResourceFile("sequences/in_DNASequenceLinearWithoutFeatures.json");
        data = converter.jsonToSequenceData(json, SequenceType.DNA);
        assertEquals("ATGGGCATCTGA", data.getSequenceString());
        assertEquals(12, data.getSequenceLength().intValue());
        assertEquals(SequenceType.DNA, data.getSequenceType());
        assertFalse(data.isCircular());
//        assertEquals(0, data.getAnnotations().size());
        assertEquals("{}", data.getAnnotations());

        assertThrows(OpenVectorEditorJsonConverterException.class,
                () -> converter.jsonToSequenceData(json, SequenceType.PROTEIN));
    }

    @Test
    public void test001b_sequenceToJsonWithDNASequenceLinearWithoutFeatures() throws Exception {
        data = SequenceData.builder()
                .sequenceString("ATGGGCATCTGA")
                .sequenceType(SequenceType.DNA)
                .circular(false)
//                .annotations(new ArrayList<>())
                .annotations("")
                .build();

        json = readResourceFile("sequences/out_DNASequenceLinearWithoutFeatures.json");
        convertedJson = converter.sequenceDataToJson(data);
        assertJsonEquals(json, convertedJson);
    }

    @Test
    public void test002a_jsonToSequenceWithDNASequenceCircularWithOneFeature() throws Exception {
        json = readResourceFile("sequences/in_DNASequenceCircularWithOneFeature.json");
        data = converter.jsonToSequenceData(json, SequenceType.DNA);
        assertEquals("ATGGGCATCTGA", data.getSequenceString());
        assertEquals(12, data.getSequenceLength().intValue());
        assertEquals(SequenceType.DNA, data.getSequenceType());
        assertTrue(data.isCircular());

        annotations = data.getAnnotations();
//        assertEquals(1, annotations.size());
//
//        annotation = annotations.get(0);
//        assertEquals(3, annotation.getStart());
//        assertEquals(8, annotation.getEnd());
//        assertEquals("My Feature ABCDEF", annotation.getName());
//        assertEquals("tag", annotation.getType());
//        assertTrue(annotation.isForward());
//        assertEquals("#E419DA", annotation.getColor());
        String expectedJson = "{\n"
                + "    \"61260d004a64b96fafe6f34c\": {\n"
                + "      \"start\": 3,\n"
                + "      \"end\": 8,\n"
                + "      \"cursorAtEnd\": true,\n"
                + "      \"id\": \"61260d004a64b96fafe6f34c\",\n"
                + "      \"forward\": true,\n"
                + "      \"type\": \"tag\",\n"
                + "      \"name\": \"My Feature ABCDEF\",\n"
                + "      \"strand\": 1,\n"
                + "      \"notes\": {},\n"
                + "      \"color\": \"#E419DA\",\n"
                + "      \"annotationTypePlural\": \"features\",\n"
                + "      \"startAngle\": 1.5707963267948966,\n"
                + "      \"endAngle\": 4.71237898038469,\n"
                + "      \"totalAngle\": 3.141582653589793,\n"
                + "      \"centerAngle\": 3.141592653589793,\n"
                + "      \"yOffset\": 0\n"
                + "    }\n"
                + "  }";
        assertJsonEquals(expectedJson, annotations);

        assertThrows(OpenVectorEditorJsonConverterException.class,
                () -> converter.jsonToSequenceData(json, SequenceType.PROTEIN));
    }

    @Test
    public void test002b_sequenceToJsonWithDNASequenceCircularWithOneFeature() throws Exception {
//        annotations = new ArrayList<>();
//        annotation = new SequenceAnnotation();
//        annotation.setStart(3);
//        annotation.setEnd(8);
//        annotation.setName("My Feature ABCDEF");
//        annotation.setType("tag");
//        annotation.setForward(true);
//        annotation.setColor("#E419DA");
//        annotations.add(annotation);
        annotations = "{\n"
                + "    \"feature_1\": {\n"
                + "      \"start\": 3,\n"
                + "      \"end\": 8,\n"
                + "      \"forward\": true,\n"
                + "      \"type\": \"tag\",\n"
                + "      \"name\": \"My Feature ABCDEF\",\n"
                + "      \"color\": \"#E419DA\"\n"
                + "    }\n"
                + "  }";
        data = SequenceData.builder()
                .sequenceString("ATGGGCATCTGA")
                .sequenceType(SequenceType.DNA)
                .circular(true)
                .annotations(annotations)
                .build();

        json = readResourceFile("sequences/out_DNASequenceCircularWithOneFeature.json");
        convertedJson = converter.sequenceDataToJson(data);
        assertJsonEquals(json, convertedJson);
    }

    @Test
    public void test003a_jsonToSequenceWithDNASequenceLinearWithThreeFeatures() throws Exception {
        json = readResourceFile("sequences/in_DNASequenceLinearWithThreeFeatures.json");
        data = converter.jsonToSequenceData(json, SequenceType.DNA);
        assertEquals("ATGGGCATCGCGTAAAGCGGTTGA", data.getSequenceString());
        assertEquals(24, data.getSequenceLength().intValue());
        assertEquals(SequenceType.DNA, data.getSequenceType());
        assertFalse(data.isCircular());

        annotations = data.getAnnotations();
//        assertEquals(3, annotations.size());
//
//        annotation = annotations.get(0);
//        assertEquals(3, annotation.getStart());
//        assertEquals(8, annotation.getEnd());
//        assertEquals("My Feature ABCDEF", annotation.getName());
//        assertEquals("tag", annotation.getType());
//        assertTrue(annotation.isForward());
//        assertEquals("#E419DA", annotation.getColor());
//
//        annotation = annotations.get(1);
//        assertEquals(6, annotation.getStart());
//        assertEquals(17, annotation.getEnd());
//        assertEquals("Second feature that overlaps with the first feature", annotation.getName());
//        assertEquals("conserved", annotation.getType());
//        assertFalse(annotation.isForward());
//        assertEquals("#A3A5F0", annotation.getColor());
//
//        annotation = annotations.get(2);
//        assertEquals(21, annotation.getStart());
//        assertEquals(23, annotation.getEnd());
//        assertEquals("Stop codon", annotation.getName());
//        assertEquals("stop", annotation.getType());
//        assertTrue(annotation.isForward());
//        assertEquals("#D44FC9", annotation.getColor());
        String expectedJson = "{\n"
                + "    \"61260d004a64b96fafe6f34c\": {\n"
                + "      \"start\": 3,\n"
                + "      \"end\": 8,\n"
                + "      \"cursorAtEnd\": true,\n"
                + "      \"id\": \"61260d004a64b96fafe6f34c\",\n"
                + "      \"forward\": true,\n"
                + "      \"type\": \"tag\",\n"
                + "      \"name\": \"My Feature ABCDEF\",\n"
                + "      \"strand\": 1,\n"
                + "      \"notes\": {},\n"
                + "      \"color\": \"#E419DA\",\n"
                + "      \"annotationTypePlural\": \"features\",\n"
                + "      \"startAngle\": 1.5707963267948966,\n"
                + "      \"endAngle\": 4.71237898038469,\n"
                + "      \"totalAngle\": 3.141582653589793,\n"
                + "      \"centerAngle\": 3.141592653589793,\n"
                + "      \"yOffset\": 0\n"
                + "    },\n"
                + "    \"61260de04a64b96fafe6f354\": {\n"
                + "      \"start\": 6,\n"
                + "      \"end\": 17,\n"
                + "      \"cursorAtEnd\": true,\n"
                + "      \"id\": \"61260de04a64b96fafe6f354\",\n"
                + "      \"forward\": false,\n"
                + "      \"type\": \"conserved\",\n"
                + "      \"name\": \"Second feature that overlaps with the first feature\",\n"
                + "      \"strand\": -1,\n"
                + "      \"notes\": {},\n"
                + "      \"color\": \"#A3A5F0\",\n"
                + "      \"annotationTypePlural\": \"features\"\n"
                + "    },\n"
                + "    \"61260e264a64b96fafe6f35a\": {\n"
                + "      \"start\": 21,\n"
                + "      \"end\": 23,\n"
                + "      \"cursorAtEnd\": true,\n"
                + "      \"id\": \"61260e264a64b96fafe6f35a\",\n"
                + "      \"forward\": true,\n"
                + "      \"type\": \"stop\",\n"
                + "      \"name\": \"Stop codon\",\n"
                + "      \"strand\": 1,\n"
                + "      \"notes\": {},\n"
                + "      \"color\": \"#D44FC9\",\n"
                + "      \"annotationTypePlural\": \"features\"\n"
                + "    }\n"
                + "  }";
        assertJsonEquals(expectedJson, annotations);

        assertThrows(OpenVectorEditorJsonConverterException.class,
                () -> converter.jsonToSequenceData(json, SequenceType.PROTEIN));
    }

    @Test
    public void test003b_sequenceToJsonWithDNASequenceLinearWithThreeFeatures() throws Exception {
//        annotations = new ArrayList<>();
//
//        annotation = new SequenceAnnotation();
//        annotation.setStart(3);
//        annotation.setEnd(8);
//        annotation.setName("My Feature ABCDEF");
//        annotation.setType("tag");
//        annotation.setForward(true);
//        annotation.setColor("#E419DA");
//        annotations.add(annotation);
//
//        annotation = new SequenceAnnotation();
//        annotation.setStart(6);
//        annotation.setEnd(17);
//        annotation.setName("Second feature that overlaps with the first feature");
//        annotation.setType("conserved");
//        annotation.setForward(false);
//        annotation.setColor("#A3A5F0");
//        annotations.add(annotation);
//
//        annotation = new SequenceAnnotation();
//        annotation.setStart(21);
//        annotation.setEnd(23);
//        annotation.setName("Stop codon");
//        annotation.setType("stop");
//        annotation.setForward(true);
//        annotation.setColor("#D44FC9");
//        annotations.add(annotation);
        annotations = "{\n"
              + "    \"feature_1\": {\n"
              + "      \"start\": 3,\n"
              + "      \"end\": 8,\n"
              + "      \"forward\": true,\n"
              + "      \"type\": \"tag\",\n"
              + "      \"name\": \"My Feature ABCDEF\",\n"
              + "      \"color\": \"#E419DA\"\n"
              + "    },\n"
              + "    \"feature_2\": {\n"
              + "      \"start\": 6,\n"
              + "      \"end\": 17,\n"
              + "      \"forward\": false,\n"
              + "      \"type\": \"conserved\",\n"
              + "      \"name\": \"Second feature that overlaps with the first feature\",\n"
              + "      \"color\": \"#A3A5F0\"\n"
              + "    },\n"
              + "    \"feature_3\": {\n"
              + "      \"start\": 21,\n"
              + "      \"end\": 23,\n"
              + "      \"forward\": true,\n"
              + "      \"type\": \"stop\",\n"
              + "      \"name\": \"Stop codon\",\n"
              + "      \"color\": \"#D44FC9\"\n"
              + "    }\n"
              + "  }";
        data = SequenceData.builder()
                .sequenceString("ATGGGCATCGCGTAAAGCGGTTGA")
                .sequenceType(SequenceType.DNA)
                .circular(false)
                .annotations(annotations)
                .build();

        json = readResourceFile("sequences/out_DNASequenceLinearWithThreeFeatures.json");
        convertedJson = converter.sequenceDataToJson(data);
        assertJsonEquals(json, convertedJson);
    }

    @Test
    public void test004a_jsonToSequenceWithProteinSequenceWithOneFeature() throws Exception {
        json = readResourceFile("sequences/in_ProteinSequenceWithOneFeature.json");
        data = converter.jsonToSequenceData(json, SequenceType.PROTEIN);
        assertEquals("MKIRQHHFVADEGFW", data.getSequenceString());
        assertEquals(15, data.getSequenceLength().intValue());
        assertEquals(SequenceType.PROTEIN, data.getSequenceType());
        assertFalse(data.isCircular());

        annotations = data.getAnnotations();
//        assertEquals(1, annotations.size());
//
//        annotation = annotations.get(0);
//        assertEquals(6, annotation.getStart());
//        assertEquals(35, annotation.getEnd());
//        assertEquals("alpha helix", annotation.getName());
//        assertEquals("misc_structure", annotation.getType());
//        assertTrue(annotation.isForward());
//        assertEquals("#B3FF00", annotation.getColor());
        String expectedJson = "{\n"
                + "    \"612610797395d7937dfecabe\": {\n"
                + "      \"start\": 6,\n"
                + "      \"end\": 35,\n"
                + "      \"cursorAtEnd\": true,\n"
                + "      \"id\": \"612610797395d7937dfecabe\",\n"
                + "      \"forward\": true,\n"
                + "      \"type\": \"misc_structure\",\n"
                + "      \"name\": \"alpha helix\",\n"
                + "      \"strand\": 1,\n"
                + "      \"notes\": {},\n"
                + "      \"color\": \"#B3FF00\",\n"
                + "      \"annotationTypePlural\": \"features\"\n"
                + "    }\n"
                + "  }";
        assertJsonEquals(expectedJson, annotations);

        assertThrows(OpenVectorEditorJsonConverterException.class,
                () -> converter.jsonToSequenceData(json, SequenceType.DNA));
        assertThrows(OpenVectorEditorJsonConverterException.class,
                () -> converter.jsonToSequenceData(json, SequenceType.RNA));
    }

    @Test
    public void test004b_sequenceToJsonWithProteinSequenceWithOneFeature() throws Exception {
//        annotations = new ArrayList<>();
//        annotation = new SequenceAnnotation();
//        annotation.setStart(6);
//        annotation.setEnd(35);
//        annotation.setName("alpha helix");
//        annotation.setType("misc_structure");
//        annotation.setForward(true);
//        annotation.setColor("#B3FF00");
//        annotations.add(annotation);
        annotations = "{\n"
              + "    \"feature_1\": {\n"
              + "      \"start\": 6,\n"
              + "      \"end\": 35,\n"
              + "      \"forward\": true,\n"
              + "      \"type\": \"misc_structure\",\n"
              + "      \"name\": \"alpha helix\",\n"
              + "      \"color\": \"#B3FF00\"\n"
              + "    }\n"
              + "  }";
        data = SequenceData.builder()
                .sequenceString("MKIRQHHFVADEGFW")
                .sequenceType(SequenceType.PROTEIN)
                .circular(false)
                .annotations(annotations)
                .build();

        json = readResourceFile("sequences/out_ProteinSequenceWithOneFeature.json");
        convertedJson = converter.sequenceDataToJson(data);
        assertJsonEquals(json, convertedJson);
    }

    private String readResourceFile(String resourceFile) throws Exception {
        return new String(Files.readAllBytes(Paths.get(this.getClass()
                .getClassLoader().getResource(resourceFile).toURI())));
    }
}