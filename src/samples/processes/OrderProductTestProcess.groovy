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
 * OrderProductTest workflow.
 *
 * @author Arne Graeper
 * @author July Karpey
 */

class OrderProductTestProcess {
    public String productId = '101570'
    public String catalogId = 'Hardware'
    public Double price = new Double('2.79')
    public Integer quantity = new Integer(3)
    public Double  totalPrice
    public String commentary

    def descriptions = {
       OrderProductTest(label_en: "Order Product",
                        label_de: "Order Product",
                        description_en: "OrderProductTest workflow.",
                        description_de: "OrderProductTest workflow.")
       productId(label_en: "Product ID", label_de: "Product ID")
    }

    def OrderProductTestProcess = {

      OrderFormWait(isStart: true, dueDate: 0, nodeInfo: 'Product Order') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)

        on('Order').to(['CheckingOrder'])
      }

      CheckingOrderFork(nodeInfo: 'Checking order:  special user commentary:  #commentary') {
        action {
            UpdateAssignees( nodes: "CheckQuantity", roles: "ADMIN" )
            UpdateAssignees( nodes: "CheckPrice", roles: "MANAGER" )
            return "Check"
        }
        on('Check').to(['CheckQuantity','CheckPrice'])
      }

      CheckQuantityWait(dueDate: 0, nodeInfo: 'Check requested quantity of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)

        on('Okay').to(['ValidateQuantity'])
      }

      ValidateQuantity() {
          action {
              println("........Check Available quantity for product '$productId' from catalog '$catalogId'")
              def availableQuantity = 2
              if (quantity > availableQuantity) {
                  println("........Requested quantity is more then available quantity: ${availableQuantity}")
                  quantity = availableQuantity
              }
              return "Okay"
          }
          on('Okay').to(['FinishChecking'])
      }

      CheckPriceWait(dueDate: 0, nodeInfo:'Check requested price of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 totalPrice: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.WRITE_READ, quantity: ConstantUtils.READ_ONLY,
                 commentary: ConstantUtils.WRITE_READ)
        on('Okay').to(['ValidatePrice'])
      }

      ValidatePrice() {
          action {
              Log(logMessage: 'Updating price for product')
              println ("........Update Price for product '$productId' from catalog '$catalogId'")

              def tax = 0.01
              if (price) {
                  price = price*1.1
              }
              if (quantity) {
                  quantity = quantity*2
              }
              totalPrice = 1000
              return "Okay"
          }
          on('Okay').to(['FinishChecking'])
      }

      FinishCheckingAndJoin() {
        action {
            println (".......Calculating total price for ordering product '$productId' from catalog '$catalogId'")
            totalPrice = price*quantity
            return "Finished"
        }
        on('Finished').to(['FinishOrder'])
      }

      FinishOrderOrJoin(isFinal: true, nodeInfo: 'Ordering of $productId in catalog $catalogId is completed.') {
        action {
          Log(logMessage: 'Finished product ordering process.')
        }
      }

   }
 }