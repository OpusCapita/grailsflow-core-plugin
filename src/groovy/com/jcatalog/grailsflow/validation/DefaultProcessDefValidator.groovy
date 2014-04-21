/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.jcatalog.grailsflow.validation

import com.jcatalog.grailsflow.utils.ConstantUtils

/**
 * Default validation for ProcessDef.  
 *
 * @author Maria Voitovich
 */
public class DefaultProcessDefValidator implements Validator {

  public ValidationResult validate(def processDef) {
    def result = new ValidationResult()
    
    if (processDef == null) {
      result.addError("Process definition is null.")
      return result;
    }
    
    if (!processDef.nodes || processDef.nodes.size() == 0) {
      result.addError("Process ${processDef.processID} has no nodes.")
      return result;
    }
    
    def finalNodes = processDef.nodes.findAll() { !it.transitions || it.transitions.size() == 0 }
    
    if (!finalNodes || finalNodes.size() == 0) {
      result.addError("Process ${processDef.processID} has no final nodes.")
    } else {
      def waitFinalNodes = finalNodes.findAll() { it.type == ConstantUtils.NODE_TYPE_WAIT }
      if (waitFinalNodes.size() > 0) {
        result.addWarning("Process ${processDef.processID} final nodes ${waitFinalNodes*.nodeID} are of 'Wait' type. These nodes will never be completed during process execution.")
      }
    }

    return result 
  }

}