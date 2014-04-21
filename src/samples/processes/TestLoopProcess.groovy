import com.jcatalog.grailsflow.utils.ConstantUtils


/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class TestLoopProcess {
    public Integer countdown  = new Integer("7")


    def descriptions = {
        TestLoop( description_en : "Tests loops support." )
    }


    def views = {
        countdown(  )
    }


    def TestLoopProcess = {

      loop () {
        action {
          Log(logMessage: "Executing iteration")
          countdown = countdown - 1 
          Log(logMessage: "${countdown} iterations left")
          if (countdown > 0) {
            return "continue"
          } else {
            return "end"
          }
        }
        on("continue").to([ "loop" ])
        on("end").to([ "finish" ])
      }

      finish() {
        action {
          Log(logMessage: "All iterations are done.")
        }
      }

    }
 }