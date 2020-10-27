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
package de.ipb_halle.lbac.search.document;

/**
 *
 * @author fmauz
 */
public class DocumentStatistic {

    public float averageWordLength;
    public int totalDocsInNode;

    public void merge(DocumentStatistic other) {
        if (totalDocsInNode + other.totalDocsInNode > 0) {
            averageWordLength = (other.averageWordLength * other.totalDocsInNode
                    + averageWordLength * totalDocsInNode)
                    / (totalDocsInNode + other.totalDocsInNode);
        }
        totalDocsInNode += other.totalDocsInNode;
    }

}
