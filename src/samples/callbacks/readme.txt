'callbacks' folder of workarea contains scripts that are called on /process/executeCallback/<scriptFileName> URL request.
For example request of /process/executeCallback/sendEvent will invoke execution of callbacks/sendEvent.groovy script


Request, params and result are passed to callback script. Callback script should parse request to get 
processKey, nodeID, event, process variables and to store them in the result parameter. 

For example:

  result.processKey = params.processID
  result.nodeID = params.nodeID
  result.variables = [productID: params.productId, catalogID: params.catalogID]


Based on the result of callback script execution ProcessController would update process variables and invoke event of appropriate node.


Parameters that are available in the callback script:
* request -- HttpServletRequest instance
* params -- String valued parameters passed to the ProcessController by request
* result -- object for storing parsed process parameters
  
Script should fill following result properties
* result.processKey -- key of process that should be executed
* result.nodeID  -- name of node that should be executed 
* result.event  -- event that should be send to the node
* result.requester (optional, default is logged user) -- ID of person that invokes event
* result.variables (optional) -- map of name->value pairs of process variables that should be updated
* result.message (optional, default is "Callback received") -- message to be send in response in case of successful execution.
* result.statusCode (optional, default is 200) -- HTTP status code to be send in response 