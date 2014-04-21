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
package com.jcatalog.grailsflow.model.process

import java.io.StringWriter
import java.io.PrintWriter

/**
 * ProcessNodeException stores exception if it occurs during node execution 
 *
 * @author Maria Voitovich
 */
class ProcessNodeException {

    String type
    String message
    String stackTrace

    static belongsTo = [ node: ProcessNode ]

    static constraints = {
         type(nullable:false)
         message(nullable:true, maxSize: 2000)
         stackTrace(nullable:true, maxSize: 2000)
    }

    public ProcessNodeException() {
    }

    public ProcessNodeException(Exception e) {
      this.type = e.class.name
      this.message = e.message?.substring(0, [e.message?.size(), 2000].min())
      if (!message && e.cause?.targetException) {
          def cause = e.cause.targetException.toString()
          message = cause.substring(0, [cause.size(), 2000].min())
      }
      def s = new StringWriter()
      def p = new PrintWriter(s)
      e.printStackTrace(p)
      p.close()
      def st = s.toString()
      this.stackTrace = st?.substring(0, [st?.size(), 2000].min())
    }

}
