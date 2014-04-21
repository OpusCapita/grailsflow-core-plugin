/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jcatalog.grailsflow.utils;

import antlr.ANTLRException;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Reader;
import java.io.StringReader;

import java.lang.reflect.ParameterizedType;


/**
 * Provides usefull methods for convert java objects to JSON presentation.
 *
 * @author Alexander Solovey
 */
public abstract class JSONConverter {
    private static final Log log = LogFactory.getLog(JSONConverter.class);

    /**
     * Converts object to JSON presentation.
     *
     * @param bean - object to convert, accept <code>null</code> values.
     *
     * @return JSON presentation of bean object, empty object if bean is
     *         <code>null</code>
     */
    public static String toJSON(Object bean) {
        String response = "{}";
        if (bean != null) {
            try {
                response = JSONMapper.toJSON(bean).render(false);
            } catch (MapperException e) {
                log.warn("Couldn't convert [" + bean + "] to JSON.", e);
            }
        }
        return response;
    }

    /**
     * Read JSON string from <code>String</code> then converts them to object of
     * <code>Class</code> class. If there are occured some errors method throws
     * <code>IllegalArgumentException</code>.
     *
     * @param json - a <code>String</code> cantaining JSON presentation of Java object.
     * @param clazz - the class to which the JSON object should be mapped.
     *
     * @return the result Java object.
     */
    public static Object fromJSON(String json, Class clazz) {
        return fromJSON(new StringReader(json), clazz);
    }

    /**
     * Read JSON string from <code>String</code> then converts them to object of
     * type which describes by <code>ParameterizedType</code> parameter. If there are
     * occured some errors method throws <code>IllegalArgumentException</code>.
     *
     * @param json - a <code>String</code> cantaining JSON presentation of Java object.
     * @param type - a type indication to which the JSON object should be mapped.
     *
     * @return the result Java object.
     *
     * @throws IllegalArgumentException when an error occurs during reading or mapping
     *         data to object.
     */
    public static Object fromJSON(String json, ParameterizedType type) {
        return fromJSON(new StringReader(json), type);
    }

    /**
     * Read JSON string from <code>Reader</code> then converts them to object of
     * <code>Class</code> class. If there are occured some errors method throws
     * <code>IllegalArgumentException</code>.
     *
     * @param reader - a reader containing JSON presentation of Java object.
     * @param clazz - the class to which the JSON object should be mapped.
     *
     * @return the result Java object.
     *
     * @throws IllegalArgumentException when an error occurs during reading or mapping
     *         data to object.
     */
    public static Object fromJSON(Reader reader, Class clazz) {
        JSONParser jsonParser = new JSONParser(reader);

        try {
            JSONValue value = jsonParser.nextValue();
            return JSONMapper.toJava(value, clazz);
        } catch (ANTLRException e) {
            // throws when exception specific for JSON reading has occured
            throw new IllegalArgumentException("Couldn't parse json string from reader ["
                + reader + "] to the JSONValue", e);
        } catch (MapperException e) {
            throw new IllegalArgumentException(
                "Couldn't convert json string from reader [" + reader
                + "] to object of [" + clazz.getName() + "] class", e);
        }
    }

    /**
     * Read JSON string from <code>Reader</code> then converts them to object of
     * type which describes by <code>ParameterizedType</code> parameter. If there are
     * occured some errors method throws <code>IllegalArgumentException</code>.
     *
     * @param reader - a reader containing JSON presentation of Java object.
     * @param type - a type indication to which the JSON object should be mapped.
     *
     * @return the result Java object.
     *
     * @throws IllegalArgumentException when an error occurs during reading or mapping
     *         data to object.
     */
    public static Object fromJSON(Reader reader, ParameterizedType type) {
        JSONParser jsonParser = new JSONParser(reader);

        try {
            JSONValue value = jsonParser.nextValue();
            return JSONMapper.toJava(value, type);
        } catch (ANTLRException e) {
            // throws when exception specific for JSON reading has occured
            throw new IllegalArgumentException("Couldn't parse json string from reader ["
                + reader + "] to the JSONValue", e);
        } catch (MapperException e) {
            throw new IllegalArgumentException(
                "Couldn't convert json string from reader [" + reader
                + "] to Java object", e);
        }
    }
}
