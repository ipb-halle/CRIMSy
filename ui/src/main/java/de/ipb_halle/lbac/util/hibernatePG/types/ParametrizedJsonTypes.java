/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Data Publica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.ipb_halle.lbac.util.hibernatePG.types;

import com.fasterxml.jackson.databind.type.SimpleType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.usertype.ParameterizedType;

import jakarta.persistence.MappedSuperclass;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

@MappedSuperclass
@TypeDefs({
    @TypeDef(typeClass = ParametrizedJsonTypes.JsonCollection.class, name = "JsonCollection"),
    @TypeDef(typeClass = ParametrizedJsonTypes.JsonList.class, name = "JsonList"),
    @TypeDef(typeClass = ParametrizedJsonTypes.JsonSet.class, name = "JsonSet"),
    @TypeDef(typeClass = ParametrizedJsonTypes.Json.class, name = "Json"),
    @TypeDef(typeClass = ParametrizedJsonTypes.JsonMap.class, name = "JsonMap"),
    @TypeDef(typeClass = ParametrizedJsonTypes.RawJson.class, name = "RawJson"),
    @TypeDef(typeClass = ParametrizedJsonTypes.RawJsonb.class, name = "RawJsonb")
})

@SuppressWarnings("unchecked")
public class ParametrizedJsonTypes {

    private static <E> Class<? extends E> getClassParameter(String parameter, Properties parameters, String defaultPrefix, Class<E> mustExtend) {
        Class<?> clazz;
        String type = (String) parameters.get(parameter);
        if (type == null) {
            throw new IllegalArgumentException("You forgot to put a @Parameter with name=" + parameter + " for this json type");
        }
        try {
            clazz = Class.forName(type);
            if (!mustExtend.isAssignableFrom(clazz)) {
                throw new ClassCastException("Type " + type + " can not be assigned to " + mustExtend.getName());
            }
        } catch (ClassNotFoundException e) {
            if (!type.contains(".")) {
                // Try java.lang
                try {
                    clazz = Class.forName(defaultPrefix + type);
                    if (!mustExtend.isAssignableFrom(clazz)) {
                        throw new ClassCastException("Type " + type + " can not be assigned to " + mustExtend.getName());
                    }
                } catch (ClassNotFoundException e1) {
                    throw new IllegalArgumentException("Impossible to find class " + type + " for a Json type implementation (" + parameter + ")");
                }
            } else {
                throw new IllegalArgumentException("Impossible to find class " + type + " for a Json type implementation (" + parameter + ")");
            }
        }
        return (Class<? extends E>) clazz;
    }

    public static class RawJson extends RawJsonType {

        public RawJson() {
            super(false);
        }
    }

    public static class RawJsonb extends RawJsonType {

        public RawJsonb() {
            super(true);
        }
    }

    public static class JsonCollection extends JsonType implements ParameterizedType {

        public JsonCollection() {
        }

        public void setParameterValues(Properties parameters) {
            Class<?> clazz = ParametrizedJsonTypes.getClassParameter("type", parameters, "java.lang.", Object.class);
            Class<? extends Collection> containerClass = ParametrizedJsonTypes.getClassParameter("container", parameters, "java.util.", Collection.class);
            boolean binary = !"false".equals(parameters.get("binary"));
            init(MAPPER.getTypeFactory().constructCollectionType(containerClass, clazz), binary);
        }
    }

    public static class JsonList extends JsonCollection {

        @Override
        public void setParameterValues(Properties parameters) {
            parameters.setProperty("container", "java.util.List");
            super.setParameterValues(parameters);
        }
    }

    public static class JsonSet extends JsonCollection {

        @Override
        public void setParameterValues(Properties parameters) {
            parameters.setProperty("container", "java.util.Set");
            super.setParameterValues(parameters);
        }
    }

    @SuppressWarnings("unchecked")
    public static class Json extends JsonType implements ParameterizedType {

        public Json() {
        }

        public void setParameterValues(Properties parameters) {
            Class<?> clazz = ParametrizedJsonTypes.getClassParameter("type", parameters, "java.lang.", Object.class);
            boolean binary = !"false".equals(parameters.get("binary"));
            init(SimpleType.construct(clazz), binary);
        }
    }

    public static class JsonMap extends JsonType implements ParameterizedType {

        public JsonMap() {
        }

        public void setParameterValues(Properties parameters) {
            Class<?> keyClass = ParametrizedJsonTypes.getClassParameter("key", parameters, "java.lang.", Object.class);
            Class<?> valueClass = ParametrizedJsonTypes.getClassParameter("value", parameters, "java.lang.", Object.class);
            boolean binary = !"false".equals(parameters.get("binary"));
            init(MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass), binary);
        }
    }
}
