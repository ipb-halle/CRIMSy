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
package de.ipb_halle.crimsy_api;

/**
 * Attribute types for selection of search fields
 *
 * @author fbroda
 */
public enum AttributeType {

    /* */
    TOPLEVEL,
    DIRECT,

    /* entity properties */
    ASSAY_TARGET,
    ASSAY_RECORD,
    COLLECTION,
    EXP_CODE,
    INSTITUTION,
    INDEX_TYPE,
    MATERIAL_TYPE,
    MEMBER,
    MEMBER_NAME,
    MOLECULE,
    PROJECT_NAME,
    REACTION_EDUCT,
    REACTION_PRODUCT,
    SEQUENCE_STRING,
    SEQUENCE_TYPE,
    TEMPLATE,
    WORDROOT,

    /* general attribute types */
    DEACTIVATED,
    PROJECT_DEACTIVATED,
    BARCODE,
    STRUCTURE,
    TEXT,
    OWNER,
    ID,
    PERM_READ,
    PERM_EDIT,
    PERM_CHOWN,
    PERM_GRANT,
    PERM_SUPER,
    PERM_CREATE,
    PERM_DELETE

}
