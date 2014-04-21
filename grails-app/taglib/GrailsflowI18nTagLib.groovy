import org.apache.commons.lang.ObjectUtils
import org.apache.commons.lang.StringUtils
import com.jcatalog.grailsflow.messagebundle.MessageBundle;

import org.springframework.web.servlet.support.RequestContextUtils as RCU;

class GrailsflowI18nTagLib {
  static namespace = "gf"

  def grailsflowMessageBundleProvider;

  /**
  * Defines message bundle in page scope.
  *
  * Required attributes:
  * var      - name of a message bundle in  page scope.
  * bundle   - name of a message bundle for loading.
  */
  def messageBundle = { attrs, body ->
    def bundlePath = attrs.bundle
    if(!bundlePath) {
        throwTagError("No attribute found for name [${attrs.bundle}] in tag [messageBundle]")
    }
    def varName = attrs.var
    if(!varName) {
        throwTagError("No attribute found for name [${attrs.var}] in tag [messageBundle]")
    }
    // getting locale
    def locale = RCU.getLocale(request)  
    // getting message bundle
    def messageBundle = grailsflowMessageBundleProvider.getMessageBundle(bundlePath, locale);
    // add message bundle to the page body scope
    pageScope."${varName}" = messageBundle
  }

  /**
  * It displays formatted message for current locale with using message attributes. It is a tag warpper
  * around method getFormatedMessage(attrs, body)
  *
  * If attribute <code>var<code> is specified - formatted message will be setted into page body scoupe
  * under name that attribute <code>var<code> specifies.
  *
  * If attribute <code>var<code> isn't specified - displays formatted message
  */
  def formatMessage = { attrs, body ->
    def message = getFormatedMessage(attrs, body, pageScope)
    if (attrs.var) {
      pageScope."${attrs.var}" = message
    } else {
      out<< message;
    }
  }

  private getFormatedMessage(attrs, body, pageScope) {
    if (!attrs.bundle) {
      throwTagError("No attribute found for name [bundle] in tag [formatMessage]")
    }
    def key = attrs.key
    if (!key) {
      throwTagError("No attribute found for name [key] in tag [formatMessage]")
    }
    def messageSource = pageScope.variables."${attrs["bundle"]}"
    if (messageSource == null || !(messageSource instanceof MessageBundle)) {
      throwTagError("No bundle loaded for name [${attrs['bundle']}] in tag [formatMessage]. Use messageBundle tag for loading bundle.")
    }
    //
    def stringArgs = attrs?.args?.collect {item ->
      ObjectUtils.toString(item)
    }
    // get formatted message
    def formattedString = messageSource.getMessage(key, (String[]) stringArgs?.toArray(new String[0]))
    if (StringUtils.isNotEmpty(formattedString)) {
      formattedString = formattedString.trim()
    }
    return formattedString;
  }
}