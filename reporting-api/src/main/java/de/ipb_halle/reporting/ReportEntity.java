/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * This class represents a report template entity, which can be instantiated in
 * a specific context.
 *
 * @author fbroda
 */
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
        return id;
    }

    /**
     * @return context of the report (usually the page or composite component
     *         displaying the report)
     */
    public String getContext() {
        return context;
    }

    /**
     * @return the name of the report
     */
    public String getName() {
        return name;
    }

    /**
     * @return the source URL for the report template
     */
    public String getSource() {
        return source;
    }

    /**
     * @param id the report id
     * @return this
     */
    public ReportEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * @param context the context of the report
     * @return this
     */
    public ReportEntity setContext(String context) {
        this.context = context;
        return this;
    }

    /**
     * @param name the report name
     * @return this
     */
    public ReportEntity setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param source the source URL of the report template
     * @return this
     */
    public ReportEntity setSource(String source) {
        this.source = source;
        return this;
    }
}
