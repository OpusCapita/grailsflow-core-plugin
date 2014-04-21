package com.jcatalog.grailsflow.messagebundle.i18n;

/**
 * Privides {@link I18nMessageBundle} by name. Name of message bundle is like following: 'com.jcatalog.bundleName'.
 * 
 * @author Roman Denisyuk
 */
public interface I18nMessageBundleProvider {

    /**
     * Returns message {@link I18nMessageBundle} implementation by it name and for
     * specified locale.
     *
     * @param bundleName name of a message bundle for example: 'com.jcatalog.bundleName'
     *
     * @return {@link I18nMessageBundle}. Message bundle with specified name should be exist.
     */
    I18nMessageBundle getI18nMessageBundle(String bundleName);
}
