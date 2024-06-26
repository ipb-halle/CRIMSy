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
package de.ipb_halle.lbac.search.lang;

import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.admission.ACObjectEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Id;

/**
 *
 * @author fblocal
 */

@AttributeOverride(name="owner", 
               column=@Column(name="besitzer_id"))
public class HorrorEntity extends ACObjectEntity {

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    @Id
    @FieldOrder(order=1)
    Integer id;
    
    @Column(name="foobar")
    @FieldOrder(order=3)
    @AttributeTag(type=AttributeType.TEXT)
    private String foo;
    
    @Basic
    @FieldOrder(order=2)
    private int anzahl;
 
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="street", 
               column=@Column(name="strasse")),
        @AttributeOverride(name="city", 
               column=@Column(name="ort"))
    })
    private HorrorAddressEntity address;
 
    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public HorrorAddressEntity getAddress() {
        return address;
    }

    public void setAddress(HorrorAddressEntity address) {
        this.address = address;
    }

}
