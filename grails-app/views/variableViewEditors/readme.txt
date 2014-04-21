Folder contains editor for the variable view parameters.

Name of template is name of the view type of variable. 
For example for view of SimpleView type will with "simpleView" template (_simpleView.gsp) be rendered. 

Parameter names for properties should have "<viewType>_<viewPropertyName>" format.

Parameters that are passed to template:
	* viewType  						View type. Cannot be null.
	* view		  						VariableView instance of corresponding type. Can be null.
