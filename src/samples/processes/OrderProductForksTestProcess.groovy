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
 * @author July Antonicheva
 */

class OrderProductForksTestProcess {
    public String productId = '101570'
    public String catalogId = 'Hardware'
    public Double price = new Double('2.79')
    public Integer quantity = new Integer(3)
    public Double  totalPrice
    public String commentary

    def descriptions = {
       OrderProductForksTest(label_en: "Order Product: forks tests",
                        label_de: "Order Product: forks tests",
                        description_en: "OrderProductForksTest workflow.",
                        description_de: "OrderProductForksTest workflow.")
       productId(label_en: "Product ID", label_de: "Product ID")
    }

    def OrderProductForksTestProcess = {

      OrderFormWait(isStart: true, dueDate: 0, nodeInfo: 'Product Order') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)
        action {
           Log(logMessage:'.... Starting ordering product ${productId}...')
           return "Order"
        }
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

      CheckQuantityFork(nodeInfo: 'Check requested quantity of product #productId in catalog $catalogId') {
        variable()
        action {
          println("........Check Available quantity for product '$productId' from catalog '$catalogId'")
          def availableQuantity = 2
          if (quantity > availableQuantity) {
              println("........Requested quantity is more then available quantity: ${availableQuantity}")
              quantity = availableQuantity
          }
          return "Okay"
        }
        on('Okay').to(['ValidateQuantity', 'CalculateQuantity'])
      }

      QuantityCheck1() {
        variable()
        action {
            println("........Quantity check: 1")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck2() {
        variable()
        action {
            println("........Quantity check: 2")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck3() {
        variable()
        action {
            println("........Quantity check: 3")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck4() {
        variable()
        action {
            println("........Quantity check: 4")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck5() {
        variable()
        action {
            println("........Quantity check: 5")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck6() {
        variable()
        action {
            println("........Quantity check: 6")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck7() {
        variable()
        action {
            println("........Quantity check: 7")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck8() {
        variable()
        action {
            println("........Quantity check: 8")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      QuantityCheck9() {
        variable()
        action {
            println("........Quantity check: 9")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      } 

      QuantityCheck10() {
        variable()
        action {
            println("........Quantity check: 10")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      } 

      QuantityCheck11() {
        variable()
        action {
            println("........Quantity check: 11")
            return "Okay"
        }
        on('Okay').to(['FinishQuantityChecking'])
      } 
     
      ValidateQuantityWait(nodeInfo: 'Validate requested quantity of product #productId in catalog $catalogId'){
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)
        action {
          println("........Validate quantity for product '$productId' from catalog '$catalogId'")
        }
        on('Okay').to(['QuantityCheck1', 'QuantityCheck2', 'QuantityCheck3', 'QuantityCheck4', 'QuantityCheck5', 'QuantityCheck6', 'QuantityCheck7', 'QuantityCheck8', 'QuantityCheck9', 'QuantityCheck10', 'QuantityCheck11' ])
      }

      CalculateQuantityWait(nodeInfo: 'Validate requested quantity of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)
        action {
          println("........Calculate quantity for product '$productId' from catalog '$catalogId'")
        }
        on('Okay').to(['FinishQuantityChecking'])
      }

      FinishQuantityCheckingAndJoin(nodeInfo: 'Finish quantity checking of product #productId in catalog $catalogId') {
        variable()
        action {
          println("........Finish Quantity Checking for product '$productId' from catalog '$catalogId'")
          return "Okay"

        }
        on('Okay').to(['FinishChecking'])
      }

      CheckPriceFork(dueDate: 0, nodeInfo:'Check requested price of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 totalPrice: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.WRITE_READ, quantity: ConstantUtils.READ_ONLY,
                 commentary: ConstantUtils.WRITE_READ)
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
        on('Okay').to(['ValidatePrice', 'CalculatePrice'])
      }

      ValidatePriceWait(nodeInfo: 'Validate requested price of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)
        action {
          println("........Valicate Price for product '$productId' from catalog '$catalogId'")
        }
        on('Okay').to(['FinishPriceChecking'])
      }


      CalculatePriceWait(nodeInfo: 'Calculate requested price of product #productId in catalog $catalogId') {
        variable(productId: ConstantUtils.READ_ONLY, catalogId: ConstantUtils.READ_ONLY,
                 price: ConstantUtils.READ_ONLY, quantity: ConstantUtils.WRITE_READ,
                 commentary: ConstantUtils.WRITE_READ)
        action {
          println("........Calculate price for product '$productId' from catalog '$catalogId'")
        }
        on('Okay').to(['FinishPriceChecking'])
      }

  
      FinishPriceCheckingAndJoin(nodeInfo: 'Finish Price checking of product #productId in catalog $catalogId') {
        variable()
        action {
          println("........Finish Price Checking for product '$productId' from catalog '$catalogId'")
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