
/**
 * Created by IntelliJ IDEA.
 * User: masha
 * Date: 01.12.2010
 * Time: 19:05:33
 * To change this template use File | Settings | File Templates.
 */
class UserProcessListController {
  def processListSearchParameters

  def index = {

    flash.searchFilter = ["type": "TestLoop"]
    flash.searchParameters = processListSearchParameters.findAll() { it.name != "type" }
    forward(controller: "processList", params: params)
  }

  def search = {

    flash.searchFilter = ["type": "TestLoop"]
    flash.searchParameters = processListSearchParameters.findAll() { it.name != "type" }
    params.max=2
    forward(controller: "processList", params: params)
  }

}
