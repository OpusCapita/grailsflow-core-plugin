<%--
Template for event message

parameters for template:
   processID
   nodeID
   events
   variables
   requester
--%>
<br/>
Reply to this email and modify following lines if necessary<br/>
<br/>
<g:if test="${events}">
  Please leave one of following lines for event in reply message:<br/>
  <g:each var="event" in="${events}">
    event=${event}<br/>
  </g:each>
  Note: if there's more than one event in the reply the first one will be used.<br/>
</g:if>
<br/>
<g:if test="${variables}">
  Update values for variables<br/>
  <g:each var="variableName" in="${variables?.keySet()}">
    ${variableName}=${variables.get(variableName)}<br/>
  </g:each>
  Note: if there's more than one value for variable in the reply the first one will be used.<br/>
</g:if>
<br/>
!!!PLEASE DON'T CHANGE FOLLOWING SERVICE INFORMATION!!! <br/>
processID=${processID}<br/>
nodeID=${nodeID}<br/>
<g:if test="${requester != null}">
requester=${requester}<br/>
</g:if>
