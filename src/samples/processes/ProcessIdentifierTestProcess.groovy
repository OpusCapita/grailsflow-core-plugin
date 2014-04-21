import com.jcatalog.grailsflow.process.Document
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
 * ProcessIdentifierTestProcess class contains not nullable ProcessIdentifier section.
 *
 * @author July Karpey
 */
class ProcessIdentifierTestProcess {
    public String  productId = "PRODUCT_100"
    public String catalogId = "CATALOG_CARS"
    public String address
    public Integer quantity
    public Boolean hasPreviewCard

    def ProcessIdentifier = ["productId", "catalogId"]

    def descriptions = {
       ProcessIdentifierTest(description_en: "Only one running instance of process for the same catalog and product is allowed.",
                             description_de: "Only one running instance of process for the same catalog and product is allowed.")
       productId(label_en: "Product ID", label_de: "Product ID")
       catalogId(label_en: "Catalog ID", label_de: "Catalog ID")
    }

    def ProcessIdentifierTestProcess = {

      AssignValuesWait(isStart: true, dueDate: 0) {
        variable(productId: ConstantUtils.WRITE_READ, catalogId: ConstantUtils.WRITE_READ,
                 address: ConstantUtils.WRITE_READ, quantity: ConstantUtils.WRITE_READ, hasPreviewCard: ConstantUtils.WRITE_READ)
        action {
            Log(logMessage: "Start values assignment.")
        }
        on('okay').to(['CheckValues'])
      }

      CheckValuesWait(dueDate: 0) {
        variable(productId: ConstantUtils.WRITE_READ, catalogId: ConstantUtils.WRITE_READ,
                 address: ConstantUtils.WRITE_READ, quantity: ConstantUtils.WRITE_READ, hasPreviewCard: ConstantUtils.WRITE_READ)
        action {
            Log(logMessage: "Check values.")
        }
        on('okay').to(['CompleteProcess'])
      }


     CompleteProcess(isFinal:true) {
        action {
            Log(logMessage: "Complete process")
        }
     }

    }
 }