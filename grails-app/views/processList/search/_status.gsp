<!--
Template parameters:
  name   -- parameter name
  value  -- parameter value

-->

<gf:messageBundle bundle="grailsflow.common" var="common"/>

<g:select name='${name}' value="${value}"
   from="${com.jcatalog.grailsflow.status.ProcessStatusEnum.values()*.value()}"
   optionValue="${{common['grailsflow.label.status.'+it]}}"
   noSelection="['':'']"></g:select>
