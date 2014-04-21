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

package com.jcatalog.grailsflow.engine.helper;

import java.io.*
import java.util.regex.*

/**
 * Class that parses Process source to get code of action section for each node
 *
 * DO NOT USE this class out of Grailsflow core since it's implementation will likely change
 *
 * @author Maria Voitovich
 */
public class ActionsCodeParser {

  public Map<String, Collection<String>> parse(String source, def nodesList){
      Stack<String> lines = readProcessCodeLines(source)

      Map<String, Collection<String>> nodesCode = new HashMap<String, Collection<String>>()

      // } that closes process class
      skipLinesUntil(lines, "^\\}\$")

      // } that closes process closure
      skipLinesUntil(lines, "^\\}\$")

      Collection<String> code = readActionCode(lines)
      String nodeID = readNodeID(lines)

      Integer index = nodesList.size()-1
      if (nodesList[index].nodeID != nodeID) {

          // here we can check if the nodeID is the next node, if some nodeIDs missed
          while (index >= 0 && nodesList[index].nodeID != nodeID) {
              index --
              // update Action code (remove useless statements)
              code = correctActionCode(code)
          }
      }

      while (code!=null && nodeID!=null) {
          index --
          nodesCode.put(nodeID, code)
          code = readActionCode(lines)
          nodeID = readNodeID(lines)

          if (nodesList[index].nodeID != nodeID) {
              // here we can check if the nodeID is the next node, if some nodeIDs missed
              while (index >= 0 && nodesList[index].nodeID != nodeID) {
                  index --
                  // update Action code (remove useless statements)
                  code = correctActionCode(code)
              }
          }
      }

    return  nodesCode
  }

  private Collection<String> correctActionCode (Collection<String> code) {
      if (!code) return null

      Stack<String> stack = new Stack<String>()
      stack.addAll(code)

      skipLinesUntil(stack, "^\\}\$")

      return stack
  }

  private Stack<String> readProcessCodeLines(String source) {
    def lines = new Stack<String>()
    source?.eachLine() { line ->
      lines.push(line.trim())
    }
    return lines
  }

  /**
   * pop lines before matching line, pop matching line. returns matching line 
   */
  private String skipLinesUntil(Stack<String> lines, String regex) {
    while (!lines.empty()) {
      String testLine = lines.pop()
      if (Pattern.matches(regex, testLine)) {
        return testLine
      } else {
      }
    }
    return null
  }

  /**
   * pop lines before matching line, pop matching line. returns collection of lines WITHOUT matching line.
   */
  private Collection<String> readLinesUntil(Stack<String> lines, String regex) {
    List<String> result = new ArrayList() 
    while (!lines.empty()) {
      String testLine = lines.pop()
      if (Pattern.matches(regex, testLine)) {
        return result.reverse()
      } else {
        result.add(testLine)
      }
    }
    return null
  }


  private Collection<String> readActionCode(Stack<String> lines) {
    // "}" that closes node closure
    skipLinesUntil(lines, "^\\}\$")

    // "}" that closes action closure
    skipLinesUntil(lines, "^\\}\$")

    // action lines until "action {"

    return readLinesUntil(lines, "^action(?:\\s*)\\{\$")

  }

  private String readNodeID(Stack<String> lines) {
    // "def nodeName([params]) {"

    def nodeLineRegex = "^([^\\(]+)\\((?:.*)\\)(?:\\s*)\\{\$"

    def nodeLine = skipLinesUntil(lines, nodeLineRegex)

    if (nodeLine != null) {
      def pattern =  Pattern.compile(nodeLineRegex)

      def matcher = pattern.matcher(nodeLine)
      matcher.matches()
      return getNodeID(matcher.group(1).trim())

    } else {
      return null
    }
  }

  private String getNodeID(String name){
    if (name == null) {
        return null
    } else if (name.endsWith("Wait")) {
        return name.substring(0, name.lastIndexOf("Wait"))
    } else if (name.endsWith("Fork")) {
        return name.substring(0, name.lastIndexOf("Fork"))
    } else if (name.endsWith("AndJoin")) {
        return name.substring(0, name.lastIndexOf("AndJoin"))
    } else if (name.endsWith("OrJoin")) {
        return name.substring(0, name.lastIndexOf("OrJoin"))
    } else if (name.endsWith("Activity")) {
        return name.substring(0, name.lastIndexOf("Activity"))
    } else {
        return name
    }
  }


}