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

import com.jcatalog.grailsflow.engine.execution.NodeExecutor
import com.jcatalog.grailsflow.actions.Action
import com.jcatalog.grailsflow.actions.ActionContext
import com.jcatalog.grailsflow.actions.ActionFactory

/**
 * Tests for NodeExecutor class.
 *
 * @author Maria Voitoich
 */
class NodeExecutorTests extends GroovyTestCase {

  void testInternalActions() {
    def closure = null

    // test simple case
    closure = {
      return "ok"
    }
    assert "ok" == executeClosure(closure, null)

    // second return should not execute
    closure = {
      return "ok"
      return "error"
    }
    assert "ok" == executeClosure(closure, null)
    
    // last value is return value
    closure = {
      "ok"
    }
    assert "ok" == executeClosure(closure, null)

    // last value is return value
    closure = {
      "error"
      "ok"
    }
    assert "ok" == executeClosure(closure, null)
    
    // inner returns should work
    closure = {
      if ("true" == "true") {
        return "valid"
      } else {
        return "invalid"
      }
    }
    assert "valid" == executeClosure(closure, null)

    // local vars should work
    closure = {
      def localVar = "ok"
      localVar+="!"
      return localVar
    }
    assert "ok!" == executeClosure(closure, null)

    // context vars should work
    closure = {
      return contextVar
    }
    assert "OK" == executeClosure(closure, [contextVar: "OK"])

  }

  void testExternalActions() {
    def closure = null

    // test execution
    closure = {
      TestAction()
      return "ok"
    }
    assert "ok" == executeClosure(closure, [:])

    // test return execution
    closure = {
      TestAction()
    }
    assert "ok" == executeClosure(closure, [:])

    // test return execution
    closure = {
      TestAction(result: "test")
    }
    assert "test" == executeClosure(closure, [:])

    // test return execution
    closure = {
      def var = TestAction(result: "test")
      return var
    }
    assert "test" == executeClosure(closure, [:])

    // test parameters update
    def variables = [var1: "var1", var2: "var2"]
    closure = {
      TestAction(varA: var1, varB: $var2)
      return "ok"
    }
    assert "ok" == executeClosure(closure, variables)
    assert variables
    assert variables["var1"] == "var1"
    assert variables["var2"] == "B"

    closure = {
      def variable1 = 'var1'
      def variable2 = variables["var2"]
      TestAction(varA: variable1, varB: variable2)
      if (variable1 == 'var1' && variable2 == 'B') {
        return "ok"
      } else {
        return "fail"
      }
    }
    assert "ok" == executeClosure(closure, [:])
  }
  
  private def executeClosure(def closure, def variables){
    def context = createActionContext(variables)
    def actionFactory = createActionFactoryMock()
    def nodeExecutor = new NodeExecutor(actionFactory: actionFactory)
    return nodeExecutor.execute(closure, context);
  }

  private ActionFactory createActionFactoryMock() {
    def actionFactory = [
      getActionClassForName : { name ->
        if (name != "TestAction") {
          return null
        } else {
          return TestAction.class
        }
      }
    ] as ActionFactory
    return actionFactory
  }
  
  private ActionContext createActionContext(def variables) {
    return new ActionContext([id: 0, nodes: []], "fakeNode", "testUser", variables?: [:], [:])
  }
  
}