import com.jcatalog.grailsflow.utils.ConstantUtils


/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class ActivityTestProcess {
    public String PROPERTY  = new String("name")
    public Integer age  = new Integer("30")
    public String name  = new String("John Smith")


    def descriptions = {
        ActivityTest( description_de : "The process starts quartz job and waits for callback.",
          description_en : "The process starts quartz job and waits for callback." )
    }


    def views = {
        PROPERTY(  )
        age(  )
        name(  )
    }


    def ActivityTestProcess = {
      printHello(dueDate: 120000) {
        action {
          if (age < 35) {
            name = "Mary Smith"
            Log(logMessage: PROPERTY)
            Log(logMessage: "${name} is younger then 35 years old")
          } else {
            Log(logMessage: "${name} is older then 35 years old")
          }
          if (name.contains("Smith")) {
            Log(logMessage: "${name} contains Smith")

          }
          CallQuartzJob(name: name, age: age, resultVarName: PROPERTY, delay: 60000)
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