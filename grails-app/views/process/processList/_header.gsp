<gf:messageBundle bundle="grailsflow.processDetails" var="processDetails"/>

<h1>${header ? processDetails['grailsflow.title.'+header]: processDetails['grailsflow.label.processList']}</h1>

<g:render plugin="grailsflow" template="/commons/messageInfo"/>
