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
package com.jcatalog.grailsflow.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.beans.factory.InitializingBean
import com.jcatalog.grailsflow.workarea.ScriptsProvider

import java.util.regex.Pattern

/**
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class GrailsflowActionFactory implements ActionFactory, InitializingBean {
    static String ACTION_SUFFIX = "Action.groovy"
    static final Pattern ACTION_SCRIPT_FILENAME_PATTERN = ~/(.+)${ACTION_SUFFIX}/
    protected Log log = LogFactory.getLog(getClass())
    ScriptsProvider scriptsProvider
    String actionsPath
    boolean useSubfolders = true
    // for private usage
    // groovy class loader for GrailsFlow action class loading
    private GroovyClassLoader loader = new GroovyClassLoader(getClass().classLoader)
    // maps action name to action file 
    private Map<String, File> actionNameToFileMap = new HashMap<String, File>()
    // map where key is action file and value is time when it was loaded last time
    private Map<File, CachedClass> actionFilesToCachedClassMap = new HashMap<File, CachedClass>()

    public void afterPropertiesSet() throws Exception {
      if (actionsPath == null) {
          throw new Exception("actionsPath property must be set for ${this.getClass()} bean.")
      }
      if (scriptsProvider == null) {
          throw new Exception("scriptsProvider property must be set for ${this.getClass()} bean.")
      }
      loader.setShouldRecompile(Boolean.TRUE) 
    }

    public Collection<String> getActionTypes() {
      def actionsFolder = scriptsProvider.getResourceFile(actionsPath)
      if (!actionsFolder.exists()) {
          actionsFolder.mkdirs()
          return []
      }
      def actions = []
      if (actionsFolder.isDirectory()) {
        def processingClosure = { file ->
          if (!file.isDirectory()) {
            def matcher = file.name =~ ACTION_SCRIPT_FILENAME_PATTERN
            if (matcher.matches()) {
              actions << matcher[0][1]
            }
          }
        }
        if (useSubfolders) {
          actionsFolder.eachFileRecurse(processingClosure)
        } else {
          actionsFolder.eachFile(processingClosure)
        }
      } else {
        log.error("Actions folder '${actionsPath}' is not a directory.")
      }
      return actions
    }

    private boolean actionClassNeedsToBeReloadedFromFile(File actionSourceFile) {
      boolean reloadClass = false;
      CachedClass cachedClass = actionFilesToCachedClassMap[actionSourceFile]
      if (!cachedClass || (cachedClass.loadTime < actionSourceFile.lastModified())) {
        reloadClass = true
      }
      return reloadClass;
    }

    public def getActionClassForName(def actionType) {
      log.debug("Action type: '${actionType}'")
      final File actionSourceFile = getActionScriptFile(actionType)
      log.debug("Action source file for action '${actionType}' is '${actionSourceFile}'")
      if (actionSourceFile && actionSourceFile.exists()) {
        if (actionClassNeedsToBeReloadedFromFile(actionSourceFile)) {
          synchronized (actionSourceFile) {
            try {
              if (actionClassNeedsToBeReloadedFromFile(actionSourceFile)) {
                log.debug("Action '${actionType}' class will be (re)loaded from file ${actionSourceFile}")
                // clearing cache
                actionFilesToCachedClassMap[actionSourceFile] = null;
                try {
                  log.debug("Loading action '${actionType}' class from file '${actionSourceFile}'")
                  def actionClass = loader.parseClass(new GroovyCodeSource(actionSourceFile), false)
                  if (Action.class.isAssignableFrom(actionClass) ) {
                    actionFilesToCachedClassMap[actionSourceFile] = new CachedClass(clazz: actionClass, loadTime: System.currentTimeMillis());
                    return actionClass
                  } else {
                    log.error("Class ${actionClass.simpleName} loaded from ${actionSourceFile.getAbsolutePath()} is not subclass of Action")
                    return null
                  }
                } catch(Throwable e) {
                  log.error("Could not instantiate ActionClass from script file ${actionSourceFile.getAbsolutePath()}", e)
                  return null
                }
              } else {
                return actionFilesToCachedClassMap[actionSourceFile].clazz
              }
            } catch (Throwable e) {
              log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", e)
              return null;
            }
          }
        } else {
          log.debug("Action '${actionType}' class will be taken from cache")
          // here class should be loaded already
          return actionFilesToCachedClassMap[actionSourceFile]?.clazz;
        }
      } else {
        log.error("Action '$actionType' source file is not found")
        return null
      }
    }

    private File getActionScriptFile(String actionName) {
      File result = actionNameToFileMap[actionName]
      if (!result) {
        synchronized(this) {
          try{
            result = actionNameToFileMap[actionName]
            if (!result) {
              File paths = scriptsProvider.getResourceFile(actionsPath)
              def scriptFileName = actionName+ACTION_SUFFIX
              if (useSubfolders) {
                paths.eachFileRecurse() {File file->
                  if (!file.isDirectory() && file.name == scriptFileName.toString()) {
                    result = file
                  }
                }
              } else {
                result = new File(paths, scriptFileName.toString())
              }
              if (result) {
                actionNameToFileMap[actionName] = result
              }
            }
          }catch (Throwable ex){
            log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
            return null;
          }
        }
      }
      return result
    }
}

protected class CachedClass {
  Class clazz;
  Long loadTime;
}
