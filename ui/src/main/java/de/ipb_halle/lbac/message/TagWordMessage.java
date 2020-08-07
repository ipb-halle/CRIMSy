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

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TagWordMessage extends Message {
    private int maxResult;

    public static class Builder extends Message.Builder<Builder> {

        private final MessageType HEADER = MessageType.TAGWORD;
        private int          maxResult = 50;
        private String       tagWord;

        /**
         * set defaults for builder
         *
         * @param - payload
         */
        public Builder() {
            super.Header(HEADER);
            super.Clazz(String.class);
        }


        public Builder setMaxResult (int maxResult){
            if (maxResult > 0) this.maxResult = maxResult;
            return this;
        }

        public Builder setTagWord(String tagWord){
            this.tagWord = Objects.requireNonNull(tagWord);
            super.body = this.tagWord;
            return this;
        }

        /**
         * build status message
         *
         * @return - status message
         */
        @Override
        public TagWordMessage build() {
            return new TagWordMessage(this);
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

    private TagWordMessage(Builder builder) {
         super(builder);
         this.maxResult = builder.maxResult;
    }
}
