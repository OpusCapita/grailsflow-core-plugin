/**
  SearchItem
  Constructor parameters:
    - name
    - searchCriterions  -- array of SelectCriterion objects
    - selectedCriterionId

  Fields:
    - select -- DOM select element for selecting search criteria
    - input  -- DOM element that contains input field(s) for selected search criteria

*/

function SearchItem (name, searchCriterions, selectedCriterionId) {
  this.select = createCriteriaSelect(name, selectedCriterionId, searchCriterions);
  this.input = document.createElement("div");

  this.inputPrototypes = buildInputPrototypesMap(searchCriterions)

  this.select.onchange = getOnChangeCriteriaFunction(this);
  if (selectedCriterionId != null) {
    this.select.value = selectedCriterionId;
  }
  this.select.onchange()
}


/*
    SearchItem helper functions
*/

function createCriteriaSelect(name, selectedCriterionId, searchCriterions) {
   var criteriaSelect = document.createElement("select");
   criteriaSelect.name=name;
   var option = null;
   var criterion = null;
   for(var i=0; i<searchCriterions.length; ++i) {
     criterion = searchCriterions[i];
     var option = document.createElement("option");
     option.value = criterion['id'];;
     option.text = criterion['label'];
     option.selected = (selectedCriterionId == option.value);
     try {
       criteriaSelect.add(option, null); // standards compliant
     } catch(ex) {
       criteriaSelect.add(option); // IE only
     }     
   }
   return criteriaSelect;
}

function buildInputPrototypesMap(searchCriterions) {
   var inputPrototypes = {};
   var criterion
   var input
   for(var i=0; i<searchCriterions.length; ++i) {
     criterion = searchCriterions[i];
     inputPrototypes[criterion['id']] = criterion['inputElements'];
   }
   return inputPrototypes;
}

function getOnChangeCriteriaFunction(searchItem) {
  return function() {
    var criteriaSelection = searchItem.select;
    var criteriaInputContainer = searchItem.input;
    var inputPrototypes = searchItem.inputPrototypes;

    var selectedIndex = Math.max(criteriaSelection.selectedIndex, 0);
    var selectedCriterionId = criteriaSelection.options[selectedIndex].value;

    for (var i = criteriaInputContainer.childNodes.length; i>0; --i){
      var child = criteriaInputContainer.childNodes[i-1];
      criteriaInputContainer.removeChild(child);
    }

    var inputPrototype = inputPrototypes[selectedCriterionId];
    for(var i=0; i<inputPrototype.length; ++i) {
      criteriaInputContainer.appendChild(inputPrototype[i].cloneNode(true));
    }
  }
}

