<g:each in="${variables}" var="varDef">
  <g:set var="variable" value="${new com.jcatalog.grailsflow.bean.BeanProxySupport([
          ['name', 'label', 'description', 'type', 'view', 'required', 'items', 'subType']: varDef,
          ['value', 'visibility']                                                         :
                  ['value'     :
                           com.jcatalog.grailsflow.model.process.ProcessVariable.getConvertedValue(varDef.defaultValue, com.jcatalog.grailsflow.model.process.ProcessVariable.defineType(varDef.type)),
                   'visibility': com.jcatalog.grailsflow.utils.ConstantUtils.WRITE_READ]
  ])}"/>
  <div class="form-group">
    <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4 control-label" for="var_${variable.name}"><gf:translatedValue translations="${variable.label}" default="${variable.name}"/></label>

    <div class="col-sm-8 col-xs-8 col-lg-8 col-md-8">
      <gf:customizingTemplate template="${variable.view?.template}" id="var_${variable.name}"
                              defaultTemplate="${com.jcatalog.grailsflow.model.view.VariableView.getDefaultTemplateForType(variable.type)}"
                              model='[variable: variable, view: variable.view, parameterName: parameterName ? parameterName : "var_${variable.name}".toString()]'/>
      <p class="help-block">
        <gf:translatedValue translations="${variable.description}" default=""/>
      </p>
    </div>
  </div>

</g:each>


