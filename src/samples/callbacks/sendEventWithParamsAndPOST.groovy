import javax.xml.parsers.*
import javax.xml.xpath.*

DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
factory.setNamespaceAware(true)
DocumentBuilder builder = factory.newDocumentBuilder()
def doc
try {
    doc = builder.parse(request.getInputStream())
} catch(SAXParseException spe) {
    result.statusCode = 500
    result.message = "Cannot parse request body"
    return 
}

def xfactory = XPathFactory.newInstance()
def xpath = xfactory.newXPath()
def expr = xpath.compile("//parameter/name/text() | //parameter/value/text()")
def nodes = expr.evaluate(doc, XPathConstants.NODESET)

def parameters = [:]
for (int i = 0; i < nodes.getLength(); i = i+2) {
    parameters[nodes.item(i).data] = nodes.item(i+1).data
}

def variables = [:]

// Fill variables form parameters
parameters?.keySet().each {variableName ->
    if (parameters[variable.name]) {
    	variables.put(variableName, parameters[variableName])
    }
}

// Check is mappingDefinitions available in XML result
xpath = xfactory.newXPath()
expr = xpath.compile("//mappingDefinitions/mapping/processVariableName/text() | //mappingDefinitions/mapping/path/text()")
nodes = expr.evaluate(doc, XPathConstants.NODESET)

if(nodes && nodes.getLength() > 0){
    //getting map [processVariableName:xPath]
    //xpath - absolute path into XML which contains value of processVariable with name processVariableName
    log.info "The ActionResult will be parsed and correspond variable will be updated."
    def mappings = [:]
    for (int i = 0; i < nodes.getLength(); i = i+2) {
        mappings[nodes.item(i).data] = nodes.item(i+1).data
    }
    mappings.each{ processVariableName, path->
        xpath = xfactory.newXPath()
        expr = xpath.compile("${path}")
        def processVariableValue = expr.evaluate(doc, XPathConstants.STRING)
        variables.put(processVariableName, processVariableValue)
    }
} else {
    //update resultVarName variable with actionresult
    xpath = xfactory.newXPath()
    expr = xpath.compile("//actionresult/text()")
    nodes = expr.evaluate(doc, XPathConstants.NODESET)
    def postResult = nodes ? nodes.item(0)?.data : null

    if (parameters.resultVarName) {
        variables.put(parameters.resultVarName, postResult)
    }
}

result.processKey = parameters.processKey
result.nodeID = parameters.nodeID
result.variables = variables 
result.event = parameters.result
result.requester = parameters.requester

                       
                       