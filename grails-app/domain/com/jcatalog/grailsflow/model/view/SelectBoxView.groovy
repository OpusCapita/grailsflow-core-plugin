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

package com.jcatalog.grailsflow.model.view

import org.springframework.util.StringUtils
/**
 * SelectBox view displays selectbox for selecting variable value. 
 * Values for selectbox are taken from <code>items</code> property.
 *
 * @author Maria Voitovich
 */
class SelectBoxView extends VariableView {
  List items

  static hasMany = [items: String]

  static transients = ["itemsString"]

  // Helper methods for setting items form UI
  String getItemsString() {
    items ? items.join(",") : null
  }

  void setItemsString(String itemsString) {
    items = itemsString ? itemsString
            .split(",").collect(){
                if (StringUtils.hasText(it)) it.trim()
                else it
            } : []
  }

}
