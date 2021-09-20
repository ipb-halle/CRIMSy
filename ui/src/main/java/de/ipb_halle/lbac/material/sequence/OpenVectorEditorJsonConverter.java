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

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Convert between JSON data from the OpenVectorEditor and {@link SequenceData}
 * objects.
 * 
 * @author flange
 */
public class OpenVectorEditorJsonConverter {
    public SequenceData jsonToSequenceData(String json,
            SequenceType sequenceType)
            throws OpenVectorEditorJsonConverterException,
            JsonProcessingException, IOException {
        SequenceData result = new SequenceData();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        JsonNode root = mapper.readTree(json);

        boolean isProteinFromJson = root.at("/isProtein").asBoolean(false);
        if (sequenceType == SequenceType.PROTEIN) {
            if (!isProteinFromJson) {
                throw new OpenVectorEditorJsonConverterException(
                        "Not a protein sequence.");
            }
            result.setSequenceString(root.at("/proteinSequence").asText());
        } else {
            if (isProteinFromJson) {
                throw new OpenVectorEditorJsonConverterException(
                        "Not a nucleotide sequence.");
            }
            result.setSequenceString(root.at("/sequence").asText());
        }
        result.setSequenceType(sequenceType);
        result.setCircular(root.at("/circular").asBoolean(false));

//        List<SequenceAnnotation> annotations = new ArrayList<>();
//        for (JsonNode featureNode : root.at("/features")) {
//            SequenceAnnotation annotation = mapper.treeToValue(featureNode,
//                    SequenceAnnotation.class);
//            annotations.add(annotation);
//        }
        String annotations = root.at("/features").toString();
        result.setAnnotations(annotations);

        return result;
    }

    public String sequenceDataToJson(SequenceData data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        if (data.getSequenceType() == SequenceType.PROTEIN) {
            root.put("isProtein", true);
            root.put("proteinSequence", data.getSequenceString());
        } else {
            root.put("sequence", data.getSequenceString());
        }
        root.put("circular", data.isCircular());

//        int i = 1;
//        for (SequenceAnnotation annotation : data.getAnnotations()) {
//            root.with("features").putPOJO("feature_" + i++, annotation);
//        }
        String annotations = data.getAnnotations();
        if ((annotations != null) && (!annotations.isEmpty())) {
            JsonNode annotationsNode = mapper.readTree(annotations);
            root.set("features", annotationsNode);
        }

        return mapper.writeValueAsString(root);
    }
}