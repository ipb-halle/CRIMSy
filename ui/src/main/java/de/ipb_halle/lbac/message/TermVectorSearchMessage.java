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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * termvector search message
 */
@XmlRootElement
public class TermVectorSearchMessage extends Message {
    private int maxResult;

    public static class Builder extends Message.Builder<Builder> {

        private final MessageType HEADER = MessageType.TERMVECTORSEARCH;
        private int          maxResult = 50;

        /**
         * set defaults for builder
         *
         * @param - payload
         */
        public Builder() {
            super.Header(HEADER);
            super.Body(new ArrayList<>());
            super.Clazz(List.class);
        }


        public Builder setMaxResult (int maxResult){
            if (maxResult > 0) this.maxResult = maxResult;
            return this;
        }

        public Builder setDocIds(List<String> docIds){
            super.Body(Objects.requireNonNull(docIds));
            return this;
        }

        /**
         * build message
         *
         * @return - termvector search message
         */
        @Override
        public TermVectorSearchMessage build() {
            return new TermVectorSearchMessage(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    //*** getter ***

    public int getMaxResult() {
        return maxResult;
    }

    public TermVectorSearchMessage(){
    }

    @SuppressWarnings("unchecked")
    public List<String> getDocIds(){
        return (List<String>) super.getBody();
    }

    private TermVectorSearchMessage(Builder builder) {
         super(builder);
         this.maxResult = builder.maxResult;
    }
}
