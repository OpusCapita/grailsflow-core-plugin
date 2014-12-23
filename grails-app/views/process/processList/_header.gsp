<gf:messageBundle bundle="grailsflow.processDetails" var="processDetails"/>
<div class="row">
  <div class="col-md-12 col-xs-12 col-lg-12">
    <h3>${processDetails['grailsflow.label.processList']}</h3>
  </div>
</div>

<div class="row">
  <div class="col-md-12 col-xs-12 col-lg-12">
    <g:render plugin="grailsflow" template="/commons/messageInfo"/>
  </div>
</div>