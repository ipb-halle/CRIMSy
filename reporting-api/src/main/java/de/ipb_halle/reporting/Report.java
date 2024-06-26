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

import de.ipb_halle.crimsy_api.DTO;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Report) {
                Report report = (Report) obj;
                return Objects.equals(id, report.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 213 + Objects.hashCode(this.id);
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

    @Override
    public String toString() {
        return "Report_" + id.toString();
    }
}
