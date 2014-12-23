<g:if test="${flash.message || flash.errors || flash.warnings}">
    <g:if test="${flash.message}">
      <div class="alert-info">
        <h4>${flash.message}</h4>
      </div>
    </g:if>

    <g:if test="${flash.errors}">
      <div class="alert-danger">
        <g:each in="${flash.errors}">
          ${it?.encodeAsHTML()}
          <br/>
        </g:each>
      </div>
    </g:if>

    <g:if test="${flash.warnings}">
      <div class="alert-warning">
        <g:each in="${flash.warnings}">
          ${it}
          <br/>
        </g:each>
      </div>
    </g:if>

</g:if>