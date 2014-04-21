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
package com.jcatalog.grailsflow.process.script

/**
 * Represents process script
 *
 * @author Maria Voitovich
 */
class ProcessScript {
    private String type
    private String source
    private Date date

    public ProcessScript(String type, String source, Date date) {
      this.type = type
      this.source = source
      this.date = date
    }

    public String getType() {
      return type
    }

    public String getSource() {
      return source
    }

    public Date getDate() {
      return date
    }

    public void setType(String type) {
      // do nothing
    }

    public void setSource(String source) {
      // do nothing
    }

    public void setDate(Date date) {
      // do nothing
    }
}