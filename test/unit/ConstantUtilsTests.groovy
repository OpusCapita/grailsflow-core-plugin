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

import com.jcatalog.grailsflow.utils.ConstantUtils

/**
 * Tests for ConstantUtils.
 *
 * @author Maria Voitoich
 */
class ConstantUtilsTests extends GroovyTestCase {

  void testNodeTypes() {
    def nodeTypes = ConstantUtils.nodeTypes
    assert nodeTypes
    assert nodeTypes instanceof Collection
    assert 5 == nodeTypes.size()
    assert nodeTypes.contains(ConstantUtils.NODE_TYPE_ACTIVITY)
    assert nodeTypes.contains(ConstantUtils.NODE_TYPE_WAIT)
    assert nodeTypes.contains(ConstantUtils.NODE_TYPE_FORK)
    assert nodeTypes.contains(ConstantUtils.NODE_TYPE_ORJOIN)
    assert nodeTypes.contains(ConstantUtils.NODE_TYPE_ANDJOIN)
  }
  
  void testEditorTypes() {
    def editorTypes = ConstantUtils.editorTypes
    assert editorTypes
    assert editorTypes instanceof Map
    assert 2 == editorTypes.size()
    def keys = editorTypes.keySet()
    assert keys.contains(ConstantUtils.EDITOR_AUTO)
    assert "EDITOR_AUTO" == editorTypes[ConstantUtils.EDITOR_AUTO]
    assert keys.contains(ConstantUtils.EDITOR_MANUAL)
    assert "EDITOR_MANUAL" == editorTypes[ConstantUtils.EDITOR_MANUAL]
  }
  
}