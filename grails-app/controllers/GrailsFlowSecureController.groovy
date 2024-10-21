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
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Base secure controller class. Used at almost all controllers for
 * providing of secure state (prohibit access to some pages and opportunity
 * to make some actions. Controller based on interceptor pattern.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
abstract class GrailsFlowSecureController {
    def securityHelper
    def beforeInterceptor = [action: this.&auth,
            except: ['index', 'login']
    ]

    private auth() {
        if (!securityHelper.isControllerAccessible(session)) {
            redirect(controller: 'login', action: 'index')
            return false
        }
    }

    /**
     * Common method for determination authorities of logged user.
     *
     * @param session
     * @return
     */
    protected def getUserAuthorities(def session) {
       return securityHelper.getUserAuthorities(session)
    }

}