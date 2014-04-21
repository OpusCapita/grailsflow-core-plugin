  <table>
    <g:each in="${variables}" var="varDef">
      <g:set var="variable" value="${new com.jcatalog.grailsflow.bean.BeanProxySupport([
        ['name', 'label', 'description', 'type', 'view', 'required', 'items', 'subType']: varDef ,
        ['value', 'visibility']:
          ['value':
            com.jcatalog.grailsflow.model.process.ProcessVariable.getConvertedValue(varDef.defaultValue, com.jcatalog.grailsflow.model.process.ProcessVariable.defineType(varDef.type)),
          'visibility' : com.jcatalog.grailsflow.utils.ConstantUtils.WRITE_READ]
      ])}"/>
      <tr>
        <td valign="top"><gf:translatedValue translations="${variable.label}" default="${variable.name}" /></td>
        <td>
          <gf:customizingTemplate template="${variable.view?.template}"
              defaultTemplate="${com.jcatalog.grailsflow.model.view.VariableView.getDefaultTemplateForType(variable.type)}"
              model='[variable: variable, view: variable.view, parameterName: parameterName ? parameterName : "var_${variable.name}".toString()]'/>
          <br/>
          <font class="hint">
            <gf:translatedValue translations="${variable.description}" default="" />
          </font>
          
        </td>
      </tr>
    </g:each>
  </table>

