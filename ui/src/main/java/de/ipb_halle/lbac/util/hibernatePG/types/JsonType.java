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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;

/**
 * Inspired by Regis Leray's impl but adjusted for the needs
 *
 * @author Loic Petit
 */
public class JsonType extends RawJsonType {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private ObjectWriter writer;
    private JavaType type;
    private ObjectReader reader;

    protected JsonType() {
    }

    @SuppressWarnings("unchecked")
    public JsonType(Class clazz, boolean isBinary) {
        this(SimpleType.construct(clazz), isBinary);
    }

    public JsonType(JavaType type, boolean isBinary) {
        init(type, isBinary);
    }

    @SuppressWarnings("unchecked")
    protected void init(JavaType type, boolean isBinary) {
        super.init(isBinary);
        this.type = type;
        this.reader = MAPPER.reader(type);
        this.writer = MAPPER.writerFor(type);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    protected Object deserialize(String content) {
        try {
            return reader.readValue(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String serialize(Object object) {
        try {
            return writer.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class returnedClass() {
        return type.getRawClass();
    }
}
