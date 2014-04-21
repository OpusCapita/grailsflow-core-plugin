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

package com.jcatalog.grailsflow.workarea

/**
 * WorkareaPathProvider interface represents provider for user defined
 * folder (workarea). Workarea - is a specified folder that is available
 * for user for different manipulations, like - saving processes
 * definitions, custom actions, storing process attachments, etc.
 *
 * The implementation of WorkareaPathProvider should have realization
 * for defined methods.
 *
 * @author Stephan Albers
 * @author July Karpey
 */

interface WorkareaPathProvider {
    String getResourcePath(String relativeResource)

    File getResourceFile(String relativeResource)

    String getResourceUrl(String relativeResource)
}