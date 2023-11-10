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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;


/**
 * Serializable data class to be used as input data of reporting jobs.
 * 
 * @author flange
 */
public class ReportDataPojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportURI;
    private ReportType type;
    private Map<String, Object> parameters;

    public ReportDataPojo(String reportURI, ReportType type, Map<String, Object> parameters) {
        this.reportURI = reportURI;
        this.type = type;
        this.parameters = parameters;
    }

    public String getReportURI() {
        return reportURI;
    }

    public ReportType getType() {
        return type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public byte[] serialize() {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
            oos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    public static ReportDataPojo deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Object o = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            o = ois.readObject();
            if (o instanceof ReportDataPojo) {
                return (ReportDataPojo) o;
            }
            throw new RuntimeException("deserialize(): illegal object type");
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
