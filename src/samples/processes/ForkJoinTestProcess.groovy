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

import com.jcatalog.grailsflow.utils.ConstantUtils

/**
 * ForkJoinTestProcess class contains fork and join nodes.
 *
 * The process has two variables: 'time' of type Date
 * and 'index' of type Integer.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ForkJoinTestProcess {
    public Date time
    public Integer index = new Integer(10)

    def descriptions = {
       ForkJoinTest(description_en: "The process demonstrates forking and join of the flow.",
                    description_de: "The process demonstrates forking and join of the flow.")
    }

    def ForkJoinTestProcess = {
    	variantsAnswerFork(isStart: true) {
  	        action {
                  Log(logMessage: 'Process forked with index #index')
                  return "okay"
  	        }
            on("okay").to(["printThanks","printRegards","printDecline","keepSilence"])
  	    }

    	printThanksWait(dueDate: 0) {
            assignees ( roles: ['ADMIN', 'SIMPLE_USER'] )
  	        action {
                Log(logMessage: "Node could be executed by #currentAssignees")
  	        }
  	        on("okay").to(["dialog"])
  	    }

    	printRegardsWait(dueDate: 0) {
            assignees ( roles: [ "ADMIN" ] )
  	        action {
                Log(logMessage: "Node could be executed by somebody from #currentAssignees")
    	        }
  	        on("okay").to(["dialog"])
  	    }

    	printDeclineWait(dueDate: 0) {
            assignees ( roles: [ "HR_USER" ] )
  	        action {
                Log(logMessage: "Node was executed by #currentAssignees")
    	        }
  	        on("okay").to(["reject"])
  	    }

    	keepSilenceWait(dueDate: 0) {
            assignees ( roles: [ "MANAGER" ] )
  	        action {
                Log(logMessage: "Node was executed by #currentAssignees")
  	        }
  	        on("okay").to(["reject"])
  	    }

    	dialogAndJoin(isFinal: true) {
  	        action {
                Log(logMessage: 'Positive dialog was finished at #{new Date()}')
  	        }
  	    }

    	rejectOrJoin(isFinal: true) {
  	        action {
                Log(logMessage: 'Rejecting was finished at #{new Date()}')
  	        }
    	}
    }
}
