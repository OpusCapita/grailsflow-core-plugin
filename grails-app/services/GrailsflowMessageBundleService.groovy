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

import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * GrailsflowMessageBundleService class is used for retrieving messages
 * from message bundle files.
 *
 * @author ami
 * 
 */

class GrailsflowMessageBundleService {
   static scope = "prototype"
   static transactional = false
   def grailsflowMessageBundleProvider

   public String getMessage(String bundle, String key){
      return getMessage(bundle, key, (String[])null)
   }

   public String getMessage(String bundle, String key, List args){
        if(args){
            return getMessage(bundle, key, (String[]) args.toArray(new String[args.size()]))
        } else {
            return getMessage(bundle, key, null)
        }
   }

   public String getMessage(String bundle, String key, String[] args){
      def locale = RCU.getLocale(RequestContextHolder.requestAttributes.request)
      return grailsflowMessageBundleProvider.getMessageBundle(bundle, locale)
          .getMessage(key, args)
   }
    
}