/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * message class for termvectors
 */
@XmlRootElement
public class TermVectorResultMessage extends Message {

    public static class Builder extends Message.Builder<Builder> {

        private final MessageType HEADER = MessageType.TERMVECTOR;

        /**
         * set defaults for builder
         *
         * @param - termvector - payload
         */
        public Builder(Map<String, Integer> termvector) {
            super.Header(HEADER);
            super.Body(termvector);
        }

        /**
         * build document message
         *
         * @return - termvector message
         */
        @Override
        public TermVectorResultMessage build() {
            return new TermVectorResultMessage(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private TermVectorResultMessage(Builder builder) {
        super(builder);
    }
}
