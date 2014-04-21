result.processKey = new Long(params.processID)
result.nodeID = params.nodeID

def variables = [:]

params.keySet().each() { paramName ->
  if (paramName.startsWith("var_")){
    def name = paramName.substring("var_".length(), paramName.length()) 
    def value = params[paramName]
    variables.put(name, value) 
  } else if (paramName.startsWith("event_")) {
    result.event = paramName.substring("event_".length(), paramName.length())
  }
}

result.variables = variables