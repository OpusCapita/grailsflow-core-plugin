import java.util.Random  

/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class LongTestProcess {

    def LongTestProcess = {

      starting() {
        action {
            return "okay"
        }
        on("okay").to("validate")
      }

      validate(){
        action {
            try {
                sleep(500000)
                return "okay"
            } catch (InterruptedException ie) {
                println("Internal thread was interrupted!")
            }
        }
        on("okay").to("ending")
      }

      ending() {
        action {
            sleep(new Random().nextInt(170) *2)
            return "okay"
        }
      }

    }
 }