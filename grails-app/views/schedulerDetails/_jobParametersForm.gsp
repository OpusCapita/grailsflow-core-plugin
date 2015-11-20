<r:require modules="grailsflowDatepicker"/>

<r:script>
   function checkSelection() {
     if (document.getElementById("repeating").value == '') {
       document.getElementById("customRepeating").style.display = ""
     } else {
       document.getElementById("customRepeating").style.display = "none"
     }
   }

   function checkRepeatInterval() {
     var interval = document.getElementById('repeating').value;
     if (interval == '') interval = document.getElementById('customRepeatingInput').value;
     var result = parseInt(interval);
     if (isNaN(interval) || result < 0) {
         alert("${g.message(code: 'plugin.grailsflow.messages.error.repeating')}");
         return false
     }

     var startHours = document.getElementById('startTime_hours').value;
     var startMinutes = document.getElementById('startTime_minutes').value;
     var intHours = parseInt(startHours);
     var intMinutes = parseInt(startMinutes);
     if (startHours == '' || isNaN(startHours) || intHours < 0 ) {
         alert("${g.message(code: 'plugin.grailsflow.messages.error.startTime')}");
         return false
     }
     if (startMinutes == '' || isNaN(startMinutes) || intMinutes < 0 ){
         alert("${g.message(code: 'plugin.grailsflow.messages.error.startTime')}");
         return false
     }

     if (result == 0) {
       if (window.confirm("${g.message(code: 'plugin.grailsflow.message.repeatInterval.zero')}") == true) {
         return true
       } else return false
     }
     return true
   }
</r:script>

<div class="form-group">
  <label class="col-md-4 control-label" for="repeating"><g:message code="plugin.grailsflow.label.repeating"/></label>

  <div class="col-md-8">
    <g:select class="form-control" id="repeating" from="${repeatingInfo}" optionKey="key" optionValue="value"
              name="repeating" value="${bean?.repeating}"
              noSelection="['': '-Specify custom value-']"
              onchange="checkSelection()"/>
  </div>
</div>

<div id="customRepeating" class="form-group" style="display: none;">
  <div class="col-md-8 col-md-offset-4">
    <div class="row">
      <div class="col-md-7">
        <input name="customRepeating" size="10" maxlength="10" id="customRepeatingInput" class="form-control"
               value="${bean?.customRepeating ? bean?.customRepeating : (bean?.repeating ? bean?.repeating : '0')}"/>
      </div>

      <div class="col-md-5">
        <div class="control-label"><g:message code="plugin.grailsflow.label.milliseconds"/></div>
      </div>
    </div>
  </div>
</div>

<r:script>
  checkSelection()
</r:script>
<h4><g:message code="plugin.grailsflow.label.startTime"/></h4>

<div class="form-group">
  <label class="col-md-4 control-label" for="startDay"><g:message code="plugin.grailsflow.label.day"/></label>

  <div class="col-md-8">
    <g:set var="startDay" value="${bean?.startDay ? bean.startDay : new Date()}"/>
    <gf:bootstrapCalendar property="startDay" value="${startDay}" />
  </div>
</div>

<div class="form-group">
  <label class="col-md-4 control-label" for="repeating"><g:message code="plugin.grailsflow.label.time"/></label>

  <div class="col-md-2">
    <input type="number" step="1" id="startTime_hours" class="form-control" name="startTime_hours" size="3"
           maxlength="2"
           value="${bean?.startTime_hours ? bean?.startTime_hours : Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? '0' + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}"/>
  </div>

  <div class="col-md-1">
    <div class="text-center">
      <label class="control-label">
        :
      </label>
    </div>
  </div>

  <div class="col-md-2">
    <input type="number" step="1" id="startTime_minutes" class="form-control" name="startTime_minutes" size="3"
           maxlength="2"
           value="${bean?.startTime_minutes ? bean?.startTime_minutes : Calendar.getInstance().get(Calendar.MINUTE) < 10 ? '0' + Calendar.getInstance().get(Calendar.MINUTE) : Calendar.getInstance().get(Calendar.MINUTE)}"/>
  </div>

  <div class="col-md-3">
    <div class="control-label">
      <g:message code="plugin.grailsflow.label.clockTime"/>
    </div>
  </div>
</div>
