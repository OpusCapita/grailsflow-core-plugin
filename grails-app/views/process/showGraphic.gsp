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

  <title>Process Graphical View</title>

  <r:require modules="grailsflowGraphics"/>
  <r:layoutResources/>
  <r:script>
     function evalJson(json) {
        try {
            return eval("(" + json + ")");
        } catch (e) {
            return null;
        }
    }

    function updateVarView(info){
      ${g.remoteFunction(controller: params['controller'], action: "saveNodesPositions",
           id: processID, params:"'positions='+info")}
    }

  </r:script>

</head>
<body>
  <g:form controller="${params['controller']}" method="POST">
    <div id="toolbar">
    </div>

    <div id="paintarea" style="position:absolute;left:15px;top:25px;width:650px;height:450px">
      &nbsp;
    </div>
    <input type="hidden" name="id" value="${processID}"/>
  </g:form>
  <r:script>
     var workflow  = new draw2d.Workflow("paintarea")
     workflow.html.style.backgroundImage="";

     var simpleToolbar = new Ext.Toolbar('toolbar')
     simpleToolbar.addButton({
         id: 'closeButton',
         text: "${g.message(code: 'plugin.grailsflow.command.close')}",
         cls: 'x-btn-text-icon scroll-bottom',
         handler: onButtonCloseClick})

     simpleToolbar.addButton({
         id: 'saveButton',
         text: "${g.message(code: 'plugin.grailsflow.command.save')}",
         cls: 'x-btn-text-icon scroll-bottom',
     handler: onButtonSaveClick})

     function onButtonCloseClick(btn, e){
         window.close()
     }

     function onButtonSaveClick(btn, e){
         var newPositions = ""
         for (var i = 0; i< workflow.getDocument().getFigures().length; i++) {
            var figure = workflow.getDocument().getFigures()[i]
            newPositions = newPositions+figure.getProperty("nodeID")+","
            newPositions = newPositions+figure.getX()+","
            newPositions = newPositions+figure.getY()+","
            newPositions = newPositions+figure.getWidth()+","
            newPositions = newPositions+figure.getHeight()

            newPositions = newPositions+";"
         }

         updateVarView(newPositions)
     }

     var obj
     var port, port2
     var transitionsData = evalJson('${raw(transitionsJson)}')
     var positions = evalJson('${raw(positions)}')
     var nodes = new Array(positions.length)

     for (var i=0; i< positions.length; i++) {
        obj = new draw2d.NodeFigure()
        obj.setDeleteable(false)
        obj.setProperty("nodeID", positions[i]["nodeID"])

        if (positions[i]["statusType"] == "${ProcessController.GRAPHIC_NODE_VISITED}" ) {
            obj.setBackgroundColor(new  draw2d.Color(134,191,142))
        } else if (positions[i]["statusType"] == "${ProcessController.GRAPHIC_NODE_ACTIVE}") {
            obj.setBackgroundColor(new  draw2d.Color(250,173,181))
        } else if (positions[i]["statusType"] == "${ProcessController.GRAPHIC_NODE_UNTOUGHED}") {
            obj.setBackgroundColor(new  draw2d.Color(206,213,210))
        }

        var x, y
        if (!${isPositionsHandled}) {
          obj.setDimension(110,70)
          obj.setContent("<b>"+positions[i]["nodeLabel"]+"</b><br/>${g.message(code: 'plugin.grailsflow.label.type')}: "+positions[i]["actionType"]+"<br/>"+positions[i]["statusTypeLabel"]+"<br/><i>"+positions[i]["knotTypeLabel"]+"</i>");
          obj.setCanDrag(true);
          x = (i-Math.floor(i/3)*3)*200+40+i*10
          y = Math.floor(i/3)*80+65+i*30
        } else {
          obj.setDimension(positions[i]["width"],
                           positions[i]["height"])
          obj.setContent("<b>"+positions[i]["nodeLabel"]+"</b><br/>${g.message(code: 'plugin.grailsflow.label.type')}: "+positions[i]["actionType"]+"<br/>"+positions[i]["statusTypeLabel"]+"<br/><i>"+positions[i]["knotTypeLabel"]+"</i>");
          obj.setCanDrag(true);
          x = positions[i]["startX"]
          y = positions[i]["startY"]
        }

        workflow.addFigure(obj, x, y);
        nodes[i] = obj
      }

      for (var i=0; i< transitionsData.length; i++) {
        var tr = transitionsData[i]
        var toNodes = tr.toNodeIDs.split(',')

        for (var k=0; k< toNodes.length; k++) {
            toNodes[k] = toNodes[k].trim()
        }

        var fromNode = null
        var toNode = new Array(toNodes.length)

        var k = 0
        for (var j=0; j< nodes.length; j++) {
          if (nodes[j].getProperty("nodeID") == tr.fromNodeID) {
              fromNode = nodes[j]
          }

          if (toNodes.contains(nodes[j].getProperty("nodeID"))) {
              toNode[k] = nodes[j]
              k = k+1
          }
        }

        for (var j=0; j< toNode.length; j++) {
          if(toNode[j]) {
            var c = new draw2d.ContextmenuConnection();
            c.setEvent(tr.onEventID)
            c.setLabel(tr.eventLabel)
            c.setSource(fromNode.getPort("output"))
            c.setTarget(toNode[j].getPort("input"))
            c.setDeleteable(false)
            workflow.addFigure(c);
            c.onOtherFigureMoved(fromNode)
          }
        }

     }
  </r:script>
  <r:layoutResources/>
</body>
</html>
