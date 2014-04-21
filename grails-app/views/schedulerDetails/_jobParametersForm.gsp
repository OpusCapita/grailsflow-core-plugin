<gf:messageBundle bundle="grailsflow.schedulerDetails" var="msgs"/>

<r:require modules="grailsflowCalendar"/>
<r:script>
   function checkSelection() {
     if (document.getElementById("repeating").value == '') {
       document.getElementById("customRepeating").style.display = ""
     } else {
       document.getElementById("customRepeating").style.display = "none"
     }
   }

   function checkRepeatInterval() {
     var interval = document.getElementById('repeating').value
     if (interval == '') interval = document.getElementById('customRepeatingInput').value
     var result = parseInt(interval)
     if (isNaN(interval) || result < 0) {
         alert("${msgs['grailsflow.messages.error.repeating']}")
         return false
     }

     var startHours = document.getElementById('startTime_hours').value
     var startMinutes = document.getElementById('startTime_minutes').value
     var intHours = parseInt(startHours)
     var intMinutes = parseInt(startMinutes)
     if (startHours == '' || isNaN(startHours) || intHours < 0 ) {
         alert("${msgs['grailsflow.messages.error.startTime']}")
         return false
     }
     if (startMinutes == '' || isNaN(startMinutes) || intMinutes < 0 ){
         alert("${msgs['grailsflow.messages.error.startTime']}")
         return false
     }

     if (result == 0) {
       if (window.confirm("${msgs['grailsflow.message.repeatInterval.zero']}") == true) {
         return true
       } else return false
     }
     return true
   }
</r:script>

  <table>
    <tr>
      <td valign="top" width="10%">${msgs['grailsflow.label.repeating']}</td>
      <td>
        <table>
          <tr>
            <td width="20%">
              <g:select id="repeating" from="${repeatingInfo}" optionKey="key" optionValue="value"
              name="repeating" value="${bean?.repeating}"
              noSelection="['':'-Specify custom value-']"
              onchange="checkSelection()"/>
            </td>
            <td>
              <div id="customRepeating" style="display: none;">
                <input name="customRepeating" size="10" maxlength="10" id="customRepeatingInput"
                  value="${bean?.customRepeating ? bean?.customRepeating : (bean?.repeating ? bean?.repeating : '0')}" />
                &nbsp;${msgs['grailsflow.label.milliseconds']}
              </div>
            </td>
          </tr>
        </table>
        <r:script>
          checkSelection()
        </r:script>
      </td>
    </tr>
    <tr>
      <td valign="top">${msgs['grailsflow.label.startTime']}</td>
      <td>
        <table>
          <tr>
            <td>${msgs['grailsflow.label.day']}</td>
            <td>
              <g:set var="startDay" value="${bean?.startDay ? bean.startDay : new Date()}"/> 
              <gf:jQueryCalendar property="startDay" value="${startDay}" />
            </td>
          </tr>
          <tr>
            <td>${msgs['grailsflow.label.time']}</td>
            <td>
              <input id="startTime_hours" name="startTime_hours" size="3" maxlength="2" value="${bean?.startTime_hours ? bean?.startTime_hours : Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? '0'+Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}"/>
              :
              <input id="startTime_minutes" name="startTime_minutes" size="3" maxlength="2" value="${bean?.startTime_minutes ? bean?.startTime_minutes : Calendar.getInstance().get(Calendar.MINUTE) < 10 ? '0'+Calendar.getInstance().get(Calendar.MINUTE) : Calendar.getInstance().get(Calendar.MINUTE)}"/>
              &nbsp;
              ${msgs['grailsflow.label.clockTime']}
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
