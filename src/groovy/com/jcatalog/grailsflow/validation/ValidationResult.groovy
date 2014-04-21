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

/**
 * Bean for representing result of validation process  
 *
 * @author Maria Voitovich
 */
class ValidationResult {
  private def warnings = []
  private def errors = []
  
  public void addError(def error){
    errors << error
  }
  
  public void addWarning(def warning){
    warnings << warning
  }

  public boolean isValid() {
    return errors.size() == 0
  }
  
  public boolean hasWarnings() {
    return warnings.size() == 0
  }
  
  public def getErrors() {
    return errors;
  }

  public def getWarnings() {
    return warnings;
  }

}