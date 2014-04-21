<g:if test="${flash.message}">
  <div class="message"  style="color: green;">${flash.message}</div>
</g:if>

<g:if test="${flash.errors}">
  <div class="errors" style="color:red;">
    <ul>
      <g:each in="${flash.errors}">
        <li>${it?.encodeAsHTML()}</li>
      </g:each>
    </ul>
  </div>
</g:if>

<g:if test="${flash.warnings}">
  <div class="warnings" style="color: #CCCC00;">
    <ul>
      <g:each in="${flash.warnings}">
        <li>${it}</li>
      </g:each>
    </ul>
  </div>
</g:if>
