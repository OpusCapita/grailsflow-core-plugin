/**
  SearchCriterion
  Constructor parameters:
    - id
    - label
    - inputElements  -- Array of DOM elements for input of selected search criteria

  Fields:
    - id
    - label
    - inputElements  -- Array of DOM elements for input of selected search criteria

*/

function SearchCriterion (id, label, inputElements) {
  this.id = id
  this.label = label
  this.inputElements = inputElements
}