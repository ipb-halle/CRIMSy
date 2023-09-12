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

import de.ipb_halle.lbac.entity.DTO;
import org.omnifaces.el.functions.Objects;

/**
 * Report DTO
 *
 * @author fbroda
 */
public class Report implements DTO {
    private Integer id;
    private String context;
    private String name;
    private String source;

    public Report(ReportEntity entity) {
        id = entity.getId();
        context = entity.getContext();
        name = entity.getName();
        source = entity.getSource();
    }

    @Override
    public ReportEntity createEntity() {
        return new ReportEntity()
                .setId(id)
                .setContext(context)
                .setName(name)
                .setSource(source);
    }

    /**
     *
     * @param other
     * @return true if both report Ids and both sources are not null and equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (! Objects.isInstance(this.getClass().getName(), other)) {
            return false;
        }

        Report report = (Report) other;
        return (other == this)
                || ((id != null)
                && id.equals(report.getId())
                && (source != null)
                && source.equals(report.getSource()));
    }

    @Override
    public int hashCode() {
        return ((id != null) ? id.hashCode() : 0)
                + ((source != null) ? source.hashCode() : 0);
    }

    public Integer getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
