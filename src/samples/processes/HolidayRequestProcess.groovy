import com.jcatalog.grailsflow.utils.ConstantUtils 


/** 
 * Please remember: to refer variable value in any expression use # symbol instead of $. 
 * Example: 'Value is #{someProcessVariable}' 
 */ 
class HolidayRequestProcess { 
    public Date holidaysEnd  
    public Date holidaysStart  
    public String hrMail
    public Boolean isApproved  
    public String logMessage  
    public String managerMail 
    public String message  
    public String requesterMail  
    public String requesterName  
    public String resolution  


    def descriptions = { 
        HolidayRequest( label_de : "Request Holiday",
          label_en : "Request Holiday",
          description_de : "The process manages the request and approval of holidays for employees.",
          description_en : "The process manages the request and approval of holidays for employees." ) 
    }


    def views = { 
        holidaysEnd(  ) 
        holidaysStart(  ) 
        hrMail(  ) 
        isApproved(  ) 
        logMessage(  ) 
        managerMail(  ) 
        message(  ) 
        requesterMail(  ) 
        requesterName(  ) 
        resolution(  ) 
    } 


    def HolidayRequestProcess = { 
      HolidayRequestFormWait(editorType: ConstantUtils.EDITOR_MANUAL) { 
        variable( message: ConstantUtils.WRITE_READ, holidaysStart: ConstantUtils.WRITE_READ, requesterMail: ConstantUtils.WRITE_READ, holidaysEnd: ConstantUtils.WRITE_READ, managerMail: ConstantUtils.WRITE_READ, hrMail: ConstantUtils.WRITE_READ, requesterName: ConstantUtils.WRITE_READ) 
        action {
          if (managerMail) {
            SendNotificationMail(messageTemplate:'/emailTemplates/HolidayRequest/managerNotification',nodeID:"ManagerApproveHolidays",mailFrom:requesterMail,mailTo: managerMail,subject:'Holiday Request')
          }
        } 
        on("submit").to([ "ManagerApproveHolidays" ]) 
      } 

      ManagerApproveHolidaysWait(dueDate: 432300000, editorType: ConstantUtils.EDITOR_MANUAL) {
        variable( message: ConstantUtils.READ_ONLY, requesterName: ConstantUtils.READ_ONLY, holidaysEnd: ConstantUtils.READ_ONLY, resolution: ConstantUtils.WRITE_READ, holidaysStart: ConstantUtils.READ_ONLY) 
        assignees( roles: [ "MANAGER", "ADMIN" ] ) 
        action { 
            Log(logMessage: 'Manager makes decision...')
        } 
        on("approve").to([ "ApprovedOperation" ]) 
        on("reject").to([ "RejectNotification" ]) 
      } 

      ApprovedOperationFork() { 
        action { 
            isApproved=true
            return "approve"
        } 
        on("approve").to([ "HRNotification", "ApproveNotification" ]) 
      } 

      HRNotification() { 
        action {
          if (hrMail) {
            SendNotificationMail(messageTemplate:'/emailTemplates/HolidayRequest/hrNotification',nodeID:"HRBook",mailFrom:managerMail,mailTo: hrMail,subject:'Holiday Request')
          }
            return "okay"
        } 
        on("okay").to([ "HRBook" ]) 
      } 

      HRBookWait(dueDate: 300000, editorType: ConstantUtils.EDITOR_MANUAL) { 
        variable( isApproved: ConstantUtils.READ_ONLY, holidaysStart: ConstantUtils.READ_ONLY, logMessage: ConstantUtils.WRITE_READ, resolution: ConstantUtils.READ_ONLY, requesterName: ConstantUtils.READ_ONLY, holidaysEnd: ConstantUtils.READ_ONLY) 
        assignees( roles: [ "ADMIN", "HR_USER" ] ) 
        action { 
            Log(logMessage: 'The hr-person has booked manager decision.')
            Log(logMessage: 'So he can enter the data to the log book.')
        } 
        on("save").to([ "ApproveFinished" ]) 
      } 

      ApproveNotification() { 
        action {
          if (requesterMail) {
            SendMail(mailFrom:managerMail,mailTo: requesterMail,subject:'Approved Notification',message: 'Your request was approved.')
          }
            return "okay"
        } 
        on("okay").to([ "ApproveFinished" ]) 
      } 

      ApproveFinishedAndJoin() { 
        action { 
            return "finish"
        } 
        on("finish").to([ "Finish" ]) 
      } 

      RejectNotification() { 
        action { 
            Log(logMessage: 'Sorry but your request was rejected')
          if (requesterMail) {
            SendMail(mailFrom:managerMail,mailTo: requesterMail,subject:'Rejected Notification',message: 'Your request was rejected.')
          }
            return "finish"
        } 
        on("finish").to([ "Finish" ]) 
      } 

      Finish() { 
        action { 
            Log(logMessage: 'Holiday request workflow is finished')
        } 
      } 

    } 
 }