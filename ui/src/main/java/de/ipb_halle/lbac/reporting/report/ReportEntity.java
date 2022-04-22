/*
 * Cloud Resource & Information Management System (CRIMSy)
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
package de.ipb_halle.lbac.reporting.report;


/**
 * This class represents a report template entity, which can be 
 * instantiated in a specific context.
 */
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "reports")
public class ReportEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column
    @NotNull
    private String context;

    @Column
    @NotNull
    private String name;

    @Column
    @NotNull
    private String source;

    /**
     * @return report id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * @return context of the report (usually the page or 
     * composite component displaying the report)
     * 
     */
    public String getContext() {
        return this.context;
    }

    /**
     * @return the name of the report 
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the source URL for the report
     */
    public String getSource() {
        return this.source;
    }

    /**
     * @param i the report id
     * @return
     */
    public ReportEntity setId(Integer i) {
        this.id = i;
        return this;
    }

    /**
     * @param c the context of the report
     * @return
     */
    public ReportEntity setContext(String c) {
        this.context = c;
        return this;
    }

    /**
     * @param n the report name
     * @return
     */
    public ReportEntity setName(String n) {
        this.name = n;
        return this;
    }

    /**
     * @param s the source for the report template
     * @return
     */
    public ReportEntity setSource(String s) {
        this.source = s;
        return this;
    }
}
