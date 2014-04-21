package com.jcatalog.grailsflow.messagebundle.i18n;

import java.util.Locale;

/**
 * @author Roman Denisyuk
 */
public interface I18nMessageBundle {
    /**
     * Get message by key.
     *
     * @param key key of a message
     * @param locale locale
     *
     * @return string message
     */
    String getMessage(String key, Locale locale);

    /**
     * Get message by key using passed parameters.
     *
     * @param key key of a message
     * @param parameters arguments for message
     * @param locale locale
     *
     * @return string message
     */
    String getMessage(String key, String[] parameters, Locale locale);
}
