package com.jcatalog.grailsflow.messagebundle.i18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation {@link I18nMessageBundleProvider} - it offers simple cache for message budles.
 * 
 * @author Roman Denisyuk
 */
public abstract class CachingI18nMessageBundleProvider implements I18nMessageBundleProvider {
    private Log log = LogFactory.getLog(CachingI18nMessageBundleProvider.class);
    private final Map<String, I18nMessageBundle> messageBundles = new HashMap<String, I18nMessageBundle>();

    public I18nMessageBundle getI18nMessageBundle(String bundleName) {
        I18nMessageBundle messageBundle = null;
        synchronized (messageBundles) {
          try{
            messageBundle = messageBundles.get(bundleName);
            if (messageBundle == null) {
                // load message bundle
                messageBundle = loadMessageBundle(bundleName);
                // store in cache
                messageBundles.put(bundleName, messageBundle);
            }
          }catch (Throwable ex){
            log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex);
          }
        }
        return messageBundle;
    }

    /**
     * Real implementation should implement method of a {@link I18nMessageBundle} loading.
     * 
     * @param bundleName name of a message bundle
     * 
     * @return loaded I18nMessageBundle
     */
    abstract protected I18nMessageBundle loadMessageBundle(String bundleName);
}
