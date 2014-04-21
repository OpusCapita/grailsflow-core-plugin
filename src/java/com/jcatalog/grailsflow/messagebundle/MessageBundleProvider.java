package com.jcatalog.grailsflow.messagebundle;

import java.util.Locale;

/**
 * Privides {@link MessageBundle} by name. Name of message bundle is like following: 'com.jcatalog.bundleName'. 
 *
 * @author Roman Denisyuk
 */
public interface MessageBundleProvider {
    /**
     * Returns message {@link MessageBundle} implementation by it name and for
     * specified locale.
     *
     * @param bundleName name of a message bundle for example: 'com.jcatalog.bundleName'
     * @param locale should be used for getting message bundle file with requested translations.
     *
     * @return {@link MessageBundle}. Message bundle with specified name should be exist.
     */
    MessageBundle getMessageBundle(String bundleName, Locale locale);
}
