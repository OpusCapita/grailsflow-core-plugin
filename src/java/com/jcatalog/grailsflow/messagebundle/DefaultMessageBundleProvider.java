package com.jcatalog.grailsflow.messagebundle;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import com.jcatalog.grailsflow.messagebundle.i18n.I18nMessageBundleProvider;
import com.jcatalog.grailsflow.messagebundle.i18n.I18nMessageBundle;


/**
 * It is default implementation of {@link MessageBundleProvider}. It uses {@link
 * com.jcatalog.grailsflow.messagebundle.i18n.I18nMessageBundleProvider} for loading message bundle by its name. Also it uses
 * Spring <code>LocaleContextHolder</code> for getting current Locale - it is used for
 * creating instance of {@link MessageBundle}.
 *
 * @author Roman Denisyuk
 *
 * @see com.jcatalog.grailsflow.messagebundle.i18n.I18nMessageBundleProvider
 * @see DefaultMessageBundle
 */
public class DefaultMessageBundleProvider implements MessageBundleProvider {
    private I18nMessageBundleProvider i18nMessageBundleProvider;

    public MessageBundle getMessageBundle(String bundleName, Locale locale) {
        // load bundle
        I18nMessageBundle i18nMessageBundle = i18nMessageBundleProvider
                .getI18nMessageBundle(bundleName);

        // return bundle using specified locale
        return new DefaultMessageBundle(i18nMessageBundle, locale);
    }

    public void setI18nMessageBundleProvider(
        I18nMessageBundleProvider i18nMessageBundleProvider) {
        this.i18nMessageBundleProvider = i18nMessageBundleProvider;
    }
}
