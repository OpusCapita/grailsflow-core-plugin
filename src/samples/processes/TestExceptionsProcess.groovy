import com.jcatalog.grailsflow.utils.ConstantUtils


/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class TestExceptionsProcess {


    def descriptions = {
        TestExceptions( description_en : "Process used for testing exceptions handling" )
    }


    def TestExceptionsProcess = {

      start() {
        action {
          return "ok"
        }
        on("ok").to([ "throwHandledException" ])
      }

      throwHandledException() {
        action {
          Exception(message: "this exception will be handled by next node")
          return "ok"
        }
        on("exception").to(["handleException"])
        on("ok").to(["finish"])
      }

      handleException() {
        action {
          Log(message: "Exception was thrown in previous node")
          actionContext.exceptions["throwHandledException"]?.each() { ex ->
            Log(message: ex.message)
          }
          return "ok"
        }
        on("ok").to(["throwUnhandledException"])
      }

      throwUnhandledException() {
        action {
          Exception(message: "this exception will not be handled. process will be killed")
        }
        on("ok").to(["finish"])
      }

      finish() {
        action {
          Log(message: "ERROR: This node should never execute, process should be killed due to exceptions.")
        }
      }


    }
 }