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
package de.ipb_halle.lbac.message;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * status message class
 */
@XmlRootElement
public class StatusMessage extends Message {

    public static class Builder extends Message.Builder<Builder> {

        private final MessageType HEADER = MessageType.STATUS;

        /**
         * set defaults for builder
         *
         * @param - payload
         */
        public Builder(String status) {
            super.Header(HEADER);
            super.Body(status);
            super.Clazz(String.class);
        }

        /**
         * build status message
         *
         * @return - status message
         */
        @Override
        public StatusMessage build() {
            return new StatusMessage(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private StatusMessage(Builder builder) {
        super(builder);
    }
}
