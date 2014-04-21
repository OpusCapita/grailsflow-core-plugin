import com.jcatalog.grailsflow.utils.ConstantUtils


/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class BuggyTestProcess {
    public Integer age  = new Integer("30")
    public String name  = new String("John Smith")


    def descriptions = {
        BuggyTest(description_en : "The process script is buggy")
    }


    def views = {
        age(  )
        name(  )
    }


    def BuggyTestProcess = {
      printHello(dueDate: 120000) {
        action {
          if () {
            Log(logMessage: "${name} is younger then 35 years old")
          } else {
            Log(logMessage: "${name} is older then 35 years old")
          }
          return okay()
        }
        on("timeout").to([ "finishGreeting" ])
        on("okay").to([ "finishGreeting" ])
      }

      finishGreeting() {
        action {
          Log(logMessage: "Process workflow has been finished successfully")
      }

    }

    }
 }