<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!DOCTYPE html>
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

         <g:render plugin="grailsflow" template="/commons/global"/>
         <gf:messageBundle bundle="grailsflow.processDetails" var="msgs"/>
         <title>${msgs['grailsflow.title.showRules']}</title>
         <r:layoutResources/>
    </head>
    <body>
        <div class="row">
          <div class="col-md-12 col-xs-12 col-lg-12">
            <b class="header">${msgs['grailsflow.title.showRules']}</b>
            <br/>
            <h2 class="headline">Available Model Beans</h2>
              Following beans are available for the page:<br/>
              <dl>
               <dt>nodeDetails</dt><dd>bean of com.jcatalog.grailsflow.bean.NodeDetails type</dd></dt>
              </dl>
            <h2 class="headline">Variables and Events Rules</h2>
            There are some rules for user manual form:<br/>
            <ol>
              <li> Variable field name attribute should have var_&lt;variableName&gt; format. For example for variable 'userName': <br/>
                <xmp><input name="var_userName" /></xmp>
              </li>
              <li> Event buttons name attribute should have event_&lt;eventID&gt; format. For example for event 'approve': <br/>
                <pre>< g:submitButton name="event_approve" value="Approve" class="button" /></pre>
              </li>
            </li>
            </ol>
            <h2 class="headline">Customer Controller Usage</h2>
            If you will use customer controller you should specify its name like:<br/><br/>
            <i>'{processID}'_'{nodeID}'</i><br/>
            <pre>< g:link controller="waitTest_waitingAnswer" action="test">New Action< /g:link></pre>
            <h2 class="headline">Controller Rules</h2>
            A code for the controller should contains sequence of action closures.
            Please see the following example of the controller code:<br/>
            <pre>def test = {
                 log.debug("Test Action")
                 redirect( controller: "processDef", action: "editTypes")
             }
            </pre>
            <br/>
            Customer controller will be saved with name:<br/><br/>
              {processID}_{nodeID}Controller

            <h2 class="headline">Multi-Step Page Rules</h2>
            The code for multi-step page should contains body for generated GSP page.
            Please see the following example of page code:<br/>
            <pre>
               <xmp>       Hello, {userName} </xmp>
            </pre>
            Or something more complex:<br/>
            <xmp>
              < g:form controller="waitTest_waitingAnswer" action="test1">
                <input type="hidden" name="processID" value="${processID?.encodeAsHTML()}"/ >
                <input type="hidden" name="nodeID" value="${nodeID?.encodeAsHTML()}"/ >
                <ul><b>Information :</b>
                   <li>
                     process ID = {processID}
                   </li>
                   <li>
                     node ID = {nodeID}
                   </li>
                 </ul>

                 < g:actionSubmit  value="Next" class="button" />
              </ g:form>
            </xmp>
            <br/>
            <h2 class="headline">Customize Return Controller and Action.</h2>
            By default after manual step  form execution you will be forwarded to the Process Details page.
            You can customize this behavior by specifying return controller and action in your custom manual page.<br/>
            For example you can set it to 'showWorklist' - it means that after
            submitting form during manual step you will be redirected to the Worklist page.
            Please see the following example:<br/>
            <xmp>
            <input type="hidden" name="resultController" value="approveWorkflow"/ >
            <input type="hidden" name="resultAction" value="showApprovalDetails"/ >
            </xmp>    
            <br/><br/>
            <input type="button" class="button" value="Close" onclick="window.close();">
        </div>
      </div>
        <r:layoutResources/>
    </body>
</html>
