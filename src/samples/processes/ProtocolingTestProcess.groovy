import java.util.Random  

/**
 * Please remember: to refer variable value in any expression use # symbol instead of $.
 * Example: 'Value is #{someProcessVariable}'
 */
class ProtocolingTestProcess {

    def ProtocolingTestProcess = {

      starting() {
        action {
            return "okay"
        }
        on("okay").to("validate1")
      }

      validate1(protocolGroup: "validation") {
        action {
            sleep(new Random().nextInt(4000) * 2)
            return "okay"
        }
        on("okay").to("validate2")
      }

      validate2(protocolGroup: "validation") {
        action {
            sleep(new Random().nextInt(4000) *2)
            return "okay"
        }
        on("okay").to("validate3")
      }

      validate3(protocolGroup: "validation") {
        action {
            sleep(new Random().nextInt(4000) * 2)
            return "okay"
        }
        on("okay").to("sendingMail")
      }

      sendingMail() {
        action {
          return "okay"
        }
        on("okay").to("exec")
      }

      exec(protocolGroup: "execution") {
        action {
            sleep(new Random().nextInt(4000) *2)
            return "okay"
        }
        on("okay").to("exec2")
      }


      exec2(protocolGroup: "execution") {
        action {
            sleep(new Random().nextInt(4000) * 2)
            return "okay"
        }
        on("okay").to("finish")
      }

      finish(protocolGroup: "finishing") {
        action {
            sleep(new Random().nextInt(4000) *2)
            return "okay"
        }
        on("okay").to("finish2")
      }


      finish2(protocolGroup: "finishing") {
        action {
            sleep(1000)
            return "okay"
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