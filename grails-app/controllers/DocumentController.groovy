/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides navigation through documents directory
 *
 * @author Stephan Albers
 * @author Alexander Shulga
 */
class DocumentController extends GrailsFlowSecureController {
    def workareaPathProvider
    def documentsPath

    private static rootUrl = ""
    private static previousFileName = ""
    
    def index = {
        redirect(action: "showDirectoryContent")
    }

    def showDirectoryContent = {
        File documentsDirectory = workareaPathProvider.getResourceFile(documentsPath)
        String currentFilePath = params.file

        File currentFile = currentFilePath ?
            new File(currentFilePath) : documentsDirectory

        Boolean isRoot = documentsDirectory == currentFile
        if (!documentsDirectory && !currentFile) {
            isRoot == Boolean.TRUE
        }

        rootUrl = documentsPath && documentsPath[-1] != "/" ? "${documentsPath}/": documentsPath

        if (!isRoot && !params.moveBack) {
            rootUrl += "$params.fileName/"
        }

        if (params.moveBack) {
            rootUrl = rootUrl.reverse().replaceFirst("$previousFileName/".reverse(),"").reverse()
        }

        previousFileName = currentFile?.name
        render(view: 'directoryContent',
            model: [currentFile: currentFile, isRoot: isRoot, rootUrl: rootUrl])
    }

}