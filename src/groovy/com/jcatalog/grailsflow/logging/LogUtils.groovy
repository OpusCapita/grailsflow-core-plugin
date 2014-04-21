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

package com.jcatalog.grailsflow.logging

import org.apache.log4j.FileAppender
import org.apache.log4j.PatternLayout
import org.apache.log4j.Logger
import org.apache.log4j.spi.Filter
import org.apache.log4j.spi.LoggingEvent


public class LogUtils {

  static public def redirectLogging(File logFile, Closure closure) {
      FileAppender appender = createFileAppender(logFile)
      if (appender) {
          Logger.getRootLogger().addAppender(appender);
      }
      try {
          return closure.call()
      } finally {
          if (appender) {
              Logger.getRootLogger().removeAppender(appender);
          }
      }
  }

  static private FileAppender createFileAppender(File logFile) {
      if (logFile != null) {
          FileAppender appender = new FileAppender();
          PrintWriter logWriter = createLogWriter(logFile);
          appender.setWriter(logWriter)
          appender.setLayout(new PatternLayout("%d [%t] %-5p (%c) - %m%n"));
          appender.setName("RedirectedLogAppender${new Date().getTime()}");
          Filter filter = new ThreadLogFilter()
          appender.addFilter(filter)
          return appender
      } else {
          return null
      }
  }

  static private PrintWriter createLogWriter(File logFile) {
      try {
          logFile.parentFile.mkdirs();
          logFile.createNewFile();
          return new PrintWriter(new FileWriter(logFile, true));
      } catch (IOException e) {
          return new PrintWriter(new StringWriter());
      }
  }

}

class ThreadLogFilter extends Filter {
    final String threadName

    public ThreadLogFilter() {
        this.threadName = Thread.currentThread().getName()
    }

    @Override
    int decide(LoggingEvent loggingEvent) {
        if (loggingEvent.threadName == threadName) {
            return ACCEPT
        }
        return DENY
    }
}


