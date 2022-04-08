/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Initial data 
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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

\connect lbac 
\connect - lbac
\set LBAC_SCHEMA_VERSION '\'00003\''

BEGIN TRANSACTION;

UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';


CREATE OR REPLACE FUNCTION getDimensionLabel (BOOLEAN, BOOLEAN, INTEGER, INTEGER) RETURNS VARCHAR 
        AS $$
        --
        -- return true if the second structure is a substructure of the first. 
        --
                DECLARE
                        zerobased       ALIAS FOR $1;
                        swapdimension   ALIAS FOR $2;
                        itemrow         ALIAS FOR $3;
                        itemcol         ALIAS FOR $4;
                        value           INTEGER;
                        v               INTEGER;
                        result          VARCHAR;
                BEGIN

                        IF swapdimension IS NULL OR zerobased IS NULL OR itemrow IS NULL THEN
                            return '';
                        END IF;

                        IF swapdimension THEN
                            value := itemrow;
                        ELSE 
                            value := itemcol;
                        END IF;

                        IF value IS NOT NULL THEN
                            value := value + 1;
                            LOOP
                                v := (value + 25) % 26;
                                result := concat(result, chr(65 + v));
                                value := value - v;
                                value := value / 26;
                                EXIT WHEN value = 0;
                            END LOOP;
                        END IF;

                        IF zerobased THEN
                            v = 0;
                        ELSE
                            v = 1;
                        END IF;

                        IF swapdimension THEN                        
                            value := itemcol;
                        ELSE
                            value := itemrow;
                        END IF;

                        RETURN concat(reverse(result), value + v);
                END;
        $$ LANGUAGE plpgsql;

CREATE TABLE reports (
    id  SERIAL NOT NULL PRIMARY KEY,
    context VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    source VARCHAR NOT NULL);

INSERT INTO reports (context, name, source) VALUES (
        'de.ipb_halle.lbac.items.bean.ItemOverviewBean',
        '{"de": "Gebinde, nach Besitzer", "en": "Items, by Owner"}',
        'file:///data/reports/itemlistByOwner.prpt'
    ),(
        'de.ipb_halle.lbac.items.bean.ItemOverviewBean',
        '{"de": "Gebinde, nach Standort", "en": "Items, by Location"}',
        'file:///data/reports/itemlistByLocation.prpt'
    ),(
        'de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean',
        '{"de": "Materialien / Strukturen", "en": "Materials / Structures"}',
        'file:///data/reports/MaterialStructureOverview.prpt'
    );

COMMIT;
