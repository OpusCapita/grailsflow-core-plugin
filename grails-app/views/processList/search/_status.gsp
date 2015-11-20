<!--
Template parameters:
  name   -- parameter name
  value  -- parameter value

-->
<g:select name='${name}' value="${value}"
   from="${com.jcatalog.grailsflow.status.ProcessStatusEnum.values()*.value()}"
   optionValue="${{g.message(code: 'plugin.grailsflow.label.status.'+it)}}"
   noSelection="['':'']"></g:select>
