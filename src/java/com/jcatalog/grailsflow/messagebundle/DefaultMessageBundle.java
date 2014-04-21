package com.jcatalog.grailsflow.messagebundle;

import com.jcatalog.grailsflow.messagebundle.i18n.I18nMessageBundle;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * @author Roman Denisyuk
 */
public class DefaultMessageBundle implements MessageBundle {
    private Locale locale;
    private I18nMessageBundle i18nMessageBundle;


    public DefaultMessageBundle(I18nMessageBundle i18nMessageBundle, Locale locale) {
        this.locale = locale;
        this.i18nMessageBundle = i18nMessageBundle;
    }

    public String getMessage(String key) {
        return i18nMessageBundle.getMessage(key, locale);
    }

    public String getMessage(String key, String[] parameters) {
        return i18nMessageBundle.getMessage(key, parameters, locale);
    }
    public Object get(Object key) {
        return getMessage((String) key);
    }

    public int size() {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport size()");
    }

    public boolean isEmpty() {
        return i18nMessageBundle == null;
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport containsKey(key)");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport containsValue(value)");
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport put(key, value)");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport remove(key)");
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport putAll(map)");
    }

    public void clear() {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport clear()");
    }

    public Set keySet() {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport keySet()");
    }

    public Collection values() {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport values()");
    }

    public Set entrySet() {
        throw new UnsupportedOperationException(getClass().getName()
            + " doesn't suport entrySet()");
    }
}
