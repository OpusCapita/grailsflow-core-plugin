package com.jcatalog.grailsflow.messagebundle.i18n;

import org.apache.commons.lang.ArrayUtils;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;


/**
 * Retireves messages from {@link ReloadableResourceBundleMessageSource} that is
 * passed via constructor.
 *
 * @author Roman Denisyuk
 */
public class SpringReloadableI18nMessageBundle implements I18nMessageBundle {
    private MessageSource messageSource;

    public SpringReloadableI18nMessageBundle(
        ReloadableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, Locale locale) {
        return getMessage(key, new String[0], locale);
    }

    /**
     * Resolve the given code and arguments as message.
     *
     * @param key the key to lookup up, for example 'label.productId'
     * @param parameters list of parameterss for a message
     * @param locale Locale
     *
     * @return the resolved message, or message code if not found
     */
    public String getMessage(String key, String[] parameters, Locale locale) {
        String message;
        try {
            message = messageSource.getMessage(key,
                    (parameters != null) ? parameters : ArrayUtils.EMPTY_STRING_ARRAY,
                    locale);
        } catch (NoSuchMessageException e) {
            message = "???" + key + "???";
        }
        return message;
    }
}
