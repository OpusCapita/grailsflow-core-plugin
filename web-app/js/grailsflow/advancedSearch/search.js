/**
  Search
  Constructor parameters:
    - name
    - searchCriterions  -- array of SelectCriterion objects

  Methods:
    - addSearchCriterion(searchCriterion) -- adds searchCriterion
    - createSearchItem(selectedCriterionId) -- Creates searchItem.
                   If 'selectedCriterionId' is specified then appropriate criterion is selected .

*/

function Search(name) {
  this.name = name
  this.searchCriterions = []

  this.addSearchCriterion = function (criterion) {
    this.searchCriterions.push(criterion)
  }

  this.addSearchItem = function (selectedCriterionId) {
    if (selectedCriterionId == null && this.searchCriterions.length > 0) {
      selectedCriterionId = this.searchCriterions[0].id;
    }
    var searchItem = new SearchItem(this.name, this.searchCriterions, selectedCriterionId);
    return searchItem;
  }

}

