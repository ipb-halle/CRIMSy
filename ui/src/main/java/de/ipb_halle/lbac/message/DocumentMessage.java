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

import de.ipb_halle.lbac.entity.Document;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * document message class for documents
 */
@XmlRootElement
public class DocumentMessage extends Message {

    public static class Builder extends Message.Builder<Builder> {
        private final MessageType HEADER = MessageType.DOCUMENT;

        /**
         * set defaults for builder
         * @param document - payload
         */
        public Builder (Document document){
            super.Header(HEADER);
            super.Body(document);
            super.Clazz(Document.class);
        }

        /**
         * build document message
         * @return - document message
         */
        @Override
        public DocumentMessage build() {
            return new DocumentMessage(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
    private DocumentMessage(Builder builder) {
        super(builder);
    }
}
