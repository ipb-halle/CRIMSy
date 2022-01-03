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

/**
 *
 * @author fmauz
 */
public enum SearchCategory {
    LABEL,
    STRUCTURE,
    NAME,
    USER,
    PROJECT,
    INDEX, // need also INDEX_TYPE?
    TYPE, // should be MATERIAL_TYPE?
    LOCATION,
    DEACTIVATED,
    TEXT,
    WORDROOT,
    COLLECTION,
    TEMPLATE,
    EXP_CODE,
    SEQUENCE_QUERY_TYPE,
    SEQUENCE_QUERY_STRING,
    SEQUENCE_TRANSLATION_TABLE,
    SEQUENCE_LIBRARY_TYPE
}
