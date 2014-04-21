<!--
Template parameters:
  value  - object

-->

<g:each var="activeNode"
  in="${value.nodes.findAll() { node -> node.status.statusID == 'ACTIVATED' } }" >
  <gf:translatedValue translations="${activeNode.label}" default="${activeNode.nodeID}"/>
  <br/>
</g:each>