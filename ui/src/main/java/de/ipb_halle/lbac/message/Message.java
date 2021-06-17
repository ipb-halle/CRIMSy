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

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

/***
 * basic class for websocket messages
 * a message contains a header, a body (payload), a class describe the payload,  message id
 * implements a static builder class (build pattern)
 */
@XmlRootElement
public abstract class Message implements Serializable {

    private final static Long serialVersionUID = 1L;

    private int         id;
    private MessageType header;
    private Object      body;
    private Class       clazz;

    public int getId() {
        return id;
    }

    public String getHeader() {
        return header.toString();
    }

    @SuppressWarnings("unchecked")
    public Object getBody() {
        return castObject(clazz, body);
    }

    /**
     * static builder, builds the member class
     *
     * @param <T>
     */
    abstract static class Builder<T extends Builder<T>> {

        int         id     = 0;
        MessageType header = MessageType.DEFAULT;
        Object      body   = new Object();
        Class       clazz  = Object.class;

        /**
         * basic build methods for id, header, body and clazz
         *
         * @param id
         * @return
         */
        public T Id(int id) {
            this.id = id;
            return self();
        }

        public T Header(String h) {
            header = MessageType.byVal(h);
            return self();
        }

        public T Header(MessageType m) {
            header = Objects.requireNonNull(m);
            return self();
        }

        public T Body(Object o) {
            body = Objects.requireNonNull(o);
            return self();
        }

        public T Clazz(Class zz) {
            clazz = zz;
            return self();
        }

        /**
         * subclasses must override
         *
         * @return - build method
         */
        abstract Message build();

        /**
         * subclasses must override method to return "this"
         *
         * @return - this
         */
        protected abstract T self();
    }

    /**
     * generic type for builder
     *
     * @param builder
     */
    @SuppressWarnings("unchecked")
    Message(Builder<?> builder) {
        id = builder.id;
        header = builder.header;
        clazz = builder.clazz;
        body = castObject(builder.clazz, builder.body);
    }

    Message(){
      //nothing
    }

    /**
     * dynamic cast for payload
     *
     * @param clazz  - describe payload
     * @param object - payload
     * @param <T>    class
     * @return - casted payload
     */
    @SuppressWarnings("unchecked")
    private <T> T castObject(Class<T> clazz, Object object) {
        return (T) object;
    }
}
