<r:require modules="grailsflow"/>
<r:require modules="bootstrap"/>

<g:set var='grailsFlowCoreConfig' value="${grailsApplication.config.grailsFlowCoreConfig}"/>
<g:if test="${grailsFlowCoreConfig?.cssFile}">
  <r:external href="${g.resource(dir:grailsFlowCoreConfig?.cssDir, file:grailsFlowCoreConfig?.cssFile)}"/>
</g:if>


