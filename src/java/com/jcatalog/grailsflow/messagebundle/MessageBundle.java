package com.jcatalog.grailsflow.messagebundle;

import java.util.Map;

/**
 * Message bundle provides messages by its keys. Also it is possible to use arguments for message.
 * 
 * @author Roman Denisyuk
 */
public interface MessageBundle extends Map {
    /**
     * Get message by key.
     *
     * @param key key of a message
     *
     * @return string message
     */
    String getMessage(String key);

    /**
     * Get message by key using passed parameters.
     *
     * @param key key of a message
     * @param parameters arguments for message
     *
     * @return string message
     */
    String getMessage(String key, String[] parameters);
}
