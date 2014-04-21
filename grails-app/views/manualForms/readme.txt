Folder contains templates for manual (of Wait type) node UI.

Name of folder should match processType.
Name of template should match nodeID.

For example HolidayRequest/_ManagerApprove.gsp template is used for rendering UI for node ManagerApprove of HolidayRequest process.

Variable parameter name should have format 'var_'${variableName}. For example:
     <input name="var_userName" value="User name"/>

Event parameter name should have format  'event_'${eventID}. For example:
     <g:submitButton name="event_approve" value="Approve"/>


Parameters that are passed to template:

required:
    * nodeDetails 		        bean of com.jcatalog.grailsflow.bean.NodeDetails type

NodeDetails has following properties:
 - properties of ProcessNode class:
       - nodeID                 String node ID
       - caller					String node caller
       - status                 FlowStatus node status
       - startedOn				Date node startedOn
       - dueOn					Date node dueOn
 - label	       				Map of languageID -> node label
 - description 					Map of languageID -> node description

 - assignees					Set<String>	of assignee IDs

 - events						Set of events beans (see below)

 - process                      Process bean (see below)

 - variables					Map of variableName -> variable bean (see below)

Process bean has following properties:
  - BasicProcess properties:
    - id
    - type
    - status
    - createdOn
    - createdBy
    - lastModifiedOn
    - lastModifiedBy
    - finishedOn
    - finishedBy
  - label			    		Map of languageID -> variable label
  - description  				Map of languageID -> variable description


Event bean has following properties
 - event                        String event ID
 - label                        Map of languageID -> event label

Variable bean has following properties:
 - ProcessVariableDef properties:
     - name			   			String variable name
     - type                     String variable type
     - label			   		Map of languageID -> variable label
     - description				Map of languageID -> variable description
     - required					Boolean variable required
     - view						VariableView view of variable
 - value						Object current variable value
 - visibility					int variable visibility for current node
