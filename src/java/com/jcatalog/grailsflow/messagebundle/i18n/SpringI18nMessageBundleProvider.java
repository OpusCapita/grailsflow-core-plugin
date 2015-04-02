package com.jcatalog.grailsflow.messagebundle.i18n;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import org.springframework.core.io.ResourceLoader;

import java.util.Iterator;
import java.util.List;


/**
 * It extends {@link CachingI18nMessageBundleProvider} for caching already loaded
 * message bundles. For loading a message bundle this implementation uses Spring
 * <code>ReloadableResourceBundleMessageSource</code> as base - then it will be used for
 * creating {@link SpringReloadableI18nMessageBundle}. Note, that {@link
 * SpringI18nMessageBundleProvider#bundlesLocation} and {@link
 * SpringI18nMessageBundleProvider#resourceLoader} should be appropriate each other,
 * i.e. resorce loader should can to load message bundle with using its name and the
 * specified base path. For more information see Soring
 * <code>ReloadableResourceBundleMessageSource</code>.
 *
 * @author Roman Denisyuk
 *
 * @see CachingI18nMessageBundleProvider
 * @see SpringReloadableI18nMessageBundle
 */
public class SpringI18nMessageBundleProvider extends CachingI18nMessageBundleProvider
    implements ResourceLoaderAware, InitializingBean
{
    private static final Log log = LogFactory.getLog(SpringI18nMessageBundleProvider.class);

    /* value of it is depends of what ResourceLoader we use */
    private String bundlesLocation;
    private List<String> bundlesLocations;
    private int cacheSeconds = -1;
    private ResourceLoader resourceLoader;

    protected I18nMessageBundle loadMessageBundle(String bundleName) {
        // create full to bundle
        String[] bundlePathes = new String[] {  };
        if (StringUtils.isNotEmpty(bundlesLocation)) {
            bundlePathes = (String[]) ArrayUtils.add(bundlePathes,
                    bundlesLocation + StringUtils.replace(bundleName, ".", "/"));
        }
        if (getBundlesLocations() != null) {
            for (Iterator it = getBundlesLocations().iterator(); it.hasNext();) {
                bundlePathes = (String[]) ArrayUtils.add(bundlePathes,
                        it.next() + StringUtils.replace(bundleName, ".", "/"));
            }
        }
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setResourceLoader(resourceLoader);
        messageSource.setBasenames(bundlePathes);
        messageSource.setCacheSeconds(cacheSeconds);
        messageSource.setFallbackToSystemLocale(false);
        return new SpringReloadableI18nMessageBundle(messageSource);
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void afterPropertiesSet() throws Exception {
        if (resourceLoader == null) {
            throw new IllegalStateException("ResourceLoader should be specified for "
                + this.getClass().getName());
        }
        if (bundlesLocation == null) {
            bundlesLocation = "";
            log.warn(
                "Property bundlesLocation isn't specified. Will be used empty value for it, but configured ResourcesLoader should can to load MessageBundle by its name.");
        }
    }

    public void setBundlesLocation(String bundlesLocation) {
        this.bundlesLocation = bundlesLocation;
    }

    public void setCacheSeconds(int cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public List<String> getBundlesLocations() {
        return bundlesLocations;
    }

    public void setBundlesLocations(List<String> bundlesLocations) {
        this.bundlesLocations = bundlesLocations;
    }
}
