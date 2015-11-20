import com.jcatalog.grailsflow.bean.ProcessDetails
import com.jcatalog.grailsflow.model.process.BasicProcess
/**
 *
 * @author Maria Voitovich
 */
class ProcessDetailsController {
  def processManagerService

  def index = {
    if (params.id != null) {
        def process = BasicProcess.get(Long.valueOf(params.id))
        if (!process) {
          throw new Exception("Process ${params.id} doesn't exist")
        }
        def processClass = processManagerService.getProcessClass(process.type)
        if (!processClass) {
          throw new Exception("Process type ${process?.type} doesn't exist")
        }
        def processDetails = new ProcessDetails(process, processClass)

        render(view: 'processDetails',
               model: [processDetails: processDetails, params: params])
    } else {
      throw new Exception("Invalid parameters")
    }

  }

}
