/**
 * Asks for confirmation. Returns true on confirm, false otherwise
 * 
 * @param message
 * @return
 */
function askConfirmation(message) {
  if(window.confirm(message)) {
	  return true;
  } else {
	  return false;
  }
}

/**
 * Check condition. If it's true returns true, otherwise shows message and returns false
 *
 * @param message
 * @return
 */
function checkCondition(condition, message) {
  if (condition) {
    return true;
  } else {
    alert(message);
    return false;
  }
}

/**
 * Trim function for String class
 */
String.prototype.trim = function() { return this.replace(/^\s+|\s+$/g, ""); };

/**
 * Escape quotes function for String class
 */
String.prototype.escapeQuotes = function() { return this.gsub("\\\\\'", "'").gsub(/[\"\']/, "\\\'"); };

/**
 * contains function for Array class
 */
Array.prototype.contains = function(obj) {
    var i, listed = false;
    for (i=0; i<this.length; i++) {
      if (this[i] === obj) {
        listed = true;
        break;
      }
    }
    return listed;
  };


/**
 * Remove all spaces from string
 * 
 * @param string
 * @return string without spaces
 */
function removeSpaces(string) {
  return string.replace(/\s+/g, ""); 
}

/**
 * Add items to the input. Skip duplications.
 * 
 * @param itemsInputId - ID of input to add items to
 * @param items - comma-separated string items to add
 * @return - result items count
 */
function addItems(itemsInputId, items) {
  var userInput = document.getElementById(itemsInputId);

  var oldItemsString = removeSpaces(userInput.value);

  items = removeSpaces(items)

  // check if there's something to add
  if (items.length > 0) {
    var addItems = items.split(",");
    // add new items
    var newItems;
    if (oldItemsString.length > 0) {
      newItems = oldItemsString.split(",").concat(addItems);
    } else {
      newItems = addItems;
    }
    // Remove duplicates
    newItems.sort();
    var newItemsString = new String(newItems[0]);
    var newItemsCount = 1;
    for (i=1; i< newItems.length; ++i) {
    	if (newItems[i].trim() != newItems[i-1]) { 
    		newItemsString=newItemsString.concat(", ", newItems[i]);
    		newItemsCount++;
    	}
    }
    // write new value to input
    userInput.value = newItemsString;
    // return result items count
    return newItemsCount;
  }
  // if nothing was added return old items count
  return  oldItemsString.length > 0 ? oldItemsString.split(",").length : 0
}

/**
 * Function  for adding option to selectbox that wraps behavior of IE that differ other browsers
 * 
 * @param selectBox - select box element to add option to
 * @param option - Option to add to select box  
 */
function addOption(selectBox, option){
	  try {
	    selectBox.add(option, null); // standards compliant; doesn't work in IE
	  }
	  catch(ex) {
	    selectBox.add(option); // IE only
	  }
}

/**
 * Function for creating copy of sample element. Returns copy with empty ID and empty style.display
 * 
 * @param sampleId - ID of sample element to create copy of
 */
function cloneSampleElement(sampleId) {
    var sampleElement = document.getElementById(sampleId)
    var cloneElement = sampleElement.cloneNode(true);
    cloneElement.id = ''
    cloneElement.style.display = ''
    return cloneElement
}

/**
 * Returns form input elements without buttons.
 * 
 * @param form - from
 */
function getFilteredInputs(form) {
  return getFilteredInputs(from, null)
}

/**
 * Returns form input elements without buttons. Includes only button with name passed in second parameter
 * 
 * @param form - from
 * @param buttonName - name of the button input to include
 */
function getFilteredInputs(form, buttonName) {
    var elements = jQuery("input", form)
    var filteredElements = new Array()
    for (i=0; i< elements.length; ++i) {
      var element = elements[i]
      if (element.type != "button" && element.type != "submit") {
        filteredElements.push(element)
      } else { // add only selected button to elements
        if (buttonName != null && element.name == buttonName) {
          filteredElements.push(element)
        }
      }
    }
    return filteredElements;
}

/**
 * Returns element of specified type which is ancestor to child element
 *
 * @param child - element that's ancestor will be searched
 * @param type - type of ancestor to search
 */
function getAncestorElementOfType(child, type) {
    if (!child) return null
    var parent = child.parentNode
    if (!parent) return null
    if (parent.nodeName.toUpperCase() == type.toUpperCase()) {
      return parent
    } else {
      return getAncestorElementOfType(parent, type)
    }
}
