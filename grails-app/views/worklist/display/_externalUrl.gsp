<!--
Template parameters:
  value  - object

-->
<gf:messageBundle bundle="grailsflow.worklist" var="worklist"/>

<gf:generateExternalUrl processNodeId="${value.id}" action="openExternalUrl" controller="process" label="${worklist['grailsflow.message.externalUrl']}"/>
