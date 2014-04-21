import com.jcatalog.grailsflow.utils.ConstantUtils 
/** 
 * TestAssignmentProcess is process for testing pre-defined and dynamic assignment. adminNode assigned to ADMIN role, dynam 
 * Please remember: if you want to use variable value in any expression - use # symbol. 
 * Example: 'Value is #SomeProcessVariable' 
 */ 
class TestAssignmentProcess { 

    public String dynamicAssignee

    def descriptions = { 
        TestAssignment(description_en: "The process demonstrates pre-defined and dynamic assignment. adminNode assigned to ADMIN role, dynamicAssigneeNode can be assigned  to role dynamicly, managerNode assigned to MANAGER role.", 
                       description_de: "The process demonstrates pre-defined and dynamic assignment. adminNode assigned to ADMIN role, dynamicAssigneeNode can be assigned to role dynamicly, managerNode assigned to MANAGER role.") 
    } 
 
    def TestAssignmentProcess = { 

      startNode(isStart: true) { 
        action { 
 
          Log(logMessage: 'Next node should be executed by ${assignees[\'adminNode\']} only')
 
          return "okay"
        } 
        on('okay').to(['adminNode']) 
      } 
    
      assignDynamicNode() { 
        action { 
          if (dynamicAssignee != null) {
            Log(logMessage: 'Next node is assigned to ${dynamicAssignee}')
            UpdateAssignees( nodes : ['dynamicAssigneeNode'] , roles: [dynamicAssignee] )
          } else {
            Log(logMessage: 'Next node assignment doesn\'t change. Node is assigned to ${assignees[\'dynamicAssigneeNode\']}')
          }
          return "okay"
        } 
        on('okay').to(['dynamicAssigneeNode']) 
      } 

      finish(isFinal: true) { 
        action { 
          Log(logMessage: 'Finish process')

        } 
      } 

      dynamicAssigneeNodeWait(dueDate: 70000, editorType: ConstantUtils.EDITOR_AUTO) { 
        variable( dynamicAssignee: ConstantUtils.READ_ONLY) 
        assignees( roles : [ 'ADMIN', 'MANAGER', 'SIMPLE_USER', 'HR_USER' ]  ) 
        action { 
 
          Log(logMessage: 'Node is executed by somebody from $currentAssignees')

        } 
        on('done').to(['managerNode']) 
      } 

      managerNodeWait(dueDate: 70000, editorType: ConstantUtils.EDITOR_AUTO) { 
        variable( ) 
        assignees( roles : [ 'MANAGER' ]  ) 
        action { 
 
          Log(logMessage: 'Node is executed by somebody from $currentAssignees')

        } 
        on('done').to(['finish']) 
      } 

      adminNodeWait(dueDate: 70000, editorType: ConstantUtils.EDITOR_AUTO) { 
        variable( dynamicAssignee: ConstantUtils.WRITE_READ) 
        assignees( roles : [ 'ADMIN' ]  ) 
        action { 
 
          Log(logMessage: 'Node is executed by somebody from $currentAssignees')

        } 
        on('assign').to(['assignDynamicNode']) 
      } 

    } 
 }