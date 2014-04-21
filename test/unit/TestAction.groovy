import com.jcatalog.grailsflow.actions.Action

public class TestAction extends Action {
  public def varA
  public def varB
  public def result = 'ok'

  public Object execute() {
    println "Executing TestAction [varA=${varA?.inspect()}, varB=${varB?.inspect()}, result=${result?.inspect()}]"
    varA = 'A'
    varB = 'B'
    return result

  }
}