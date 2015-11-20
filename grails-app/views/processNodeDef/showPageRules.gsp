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
         <title><g:message code="plugin.grailsflow.title.showRules"/></title>
         <r:layoutResources/>
    </head>
    <body>
      <div class="container">
        <div class="row" style="margin-top: 10px;">
          <div class="col-md-12">
            <div class="panel panel-default">
              <div class="panel-heading"><h1><g:message code="plugin.grailsflow.title.showRules"/></h1></div>
              <div class="panel-body">

                <h1>Available Model Beans</h1>
                  Following beans are available for the page:<br/>
                  <dl>
                   <dt>nodeDetails</dt>bean of com.jcatalog.grailsflow.bean.NodeDetails type
                  </dl>
                <h3>Variables and Events Rules</h3>
                There are some rules for user manual form:<br/>
                <ol>
                  <li> Variable field name attribute should have var_&lt;variableName&gt; format. For example for variable 'userName': <br/>
                    <pre>&lt;input name="var_userName" /&gt;</pre>
                  </li>
                  <li> Event buttons name attribute should have event_&lt;eventID&gt; format. For example for event 'approve': <br/>
                    <pre>&lt;g:submitButton name="event_approve" value="Approve" class="button" /&gt;</pre>
                  </li>
                </li>
                </ol>
                <h1>Customer Controller Usage</h1>
                If you will use customer controller you should specify its name like:<br/><br/>
                <i>'{processID}'_'{nodeID}'</i><br/>
                <pre>&lt;g:link controller="waitTest_waitingAnswer" action="test"&gt;New Action&lt;/g:link&gt;</pre>
                <h3>Controller Rules</h3>
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

                <h1>Multi-Step Page Rules</h1>
                The code for multi-step page should contains body for generated GSP page.
                Please see the following example of page code:<br/>
                <pre>
                   <xmp>       Hello, {userName} </xmp>
                </pre>
                Or something more complex:<br/>
                <pre>
      &lt;g:form controller="waitTest_waitingAnswer" action="test1"&gt;
        &lt;input type="hidden" name="processID" value="${processID?.encodeAsHTML()}"/&gt;
        &lt;input type="hidden" name="nodeID" value="${nodeID?.encodeAsHTML()}"/&gt;
        &lt;ul&gt;&lt;b&gt;Information :&lt;/b&gt;
          &lt;li&gt;
             process ID = {processID}
          &lt;/li&gt;
          &lt;li&gt;
             node ID = {nodeID}
          &lt;/li&gt;
        &lt;/ul&gt;
        &lt;g:actionSubmit  value="Next" class="button" /&gt;
      &lt;/g:form&gt;
                </pre>
                <br/>
                <h1>Customize Return Controller and Action.</h1>
                By default after manual step  form execution you will be forwarded to the Process Details page.
                You can customize this behavior by specifying return controller and action in your custom manual page.<br/>
                For example you can set it to 'showWorklist' - it means that after
                submitting form during manual step you will be redirected to the Worklist page.
                Please see the following example:<br/>
                <pre>
      &lt;input type="hidden" name="resultController" value="approveWorkflow"/&gt;
      &lt;input type="hidden" name="resultAction" value="showApprovalDetails"/&gt;
                </pre>
                <br/><br/>
                <input type="button" class="btn btn-primary" value="Close" onclick="window.close();">
              </div>
            </div>
        </div>
      </div>
        <r:layoutResources/>
     </div>
    </body>
</html>
