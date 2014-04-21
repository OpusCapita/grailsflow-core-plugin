Folder contains templates that are used to display process variables on the UI.

Name of template is name of the view type of variable. 
For example for variable with view of SimpleView type will be rendered with "simpleView" template (_simpleView.gsp) 

Parameters that are passed to template:

required:	
	* variable  			variable bean object. Must be not null.
	* view                  VariableView object.  Must be not null and of corresponding type.

optional:
	* parameterName					'name' attribute for variable input. Default is empty.
	
	
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
	