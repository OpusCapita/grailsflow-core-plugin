/**
  SearchTable
  Constructor parameters:
    - tableId
    - criteriaParameterName

  Methods
    - addSearchCriterion(id, label, input)
    - setIcons(addIcon, deleteIcon)
    - addCriteriaRow(selectedCriterionId)
*/

function SearchTable(tableId, criteriaParameterName) {
  this.table = document.getElementById(tableId);

  this.addSearchCriterion = function(id, label, input) {
    this.search.addSearchCriterion(new SearchCriterion(id, label, input));
  }

  this.setIcons = function (addIcon, deleteIcon) {
    this.addIcon = addIcon;
    this.deleteIcon = deleteIcon;
  }

  this.addCriteriaRow = function(selectedCriterionId) {
    var criteriaRow = this.table.insertRow(this.table.rows.length);

    var criteriaItem = this.search.addSearchItem(selectedCriterionId);

    var criteriaCell = criteriaRow.insertCell(0);
    criteriaCell.appendChild(criteriaItem.select);

    var criteriaInputCell = criteriaRow.insertCell(1);
    criteriaInputCell.appendChild(criteriaItem.input);

    var criteriaActionsCell = criteriaRow.insertCell(2);
    if (criteriaRow.rowIndex != 0) {
      criteriaActionsCell.appendChild(this.createRemoveCriteriaLink(criteriaRow));
      criteriaActionsCell.appendChild(document.createTextNode(" "));
    }
    criteriaActionsCell.appendChild(this.createAddCriteriaLink());
  }

/*
    SearchTable private members
*/
  this.search = new Search(criteriaParameterName);
  this.addIcon = document.createElement("img");
  this.deleteIcon = document.createElement("img");

  this.removeCriteriaRow = function removeCriteriaRow(criteriaRow) {
    criteriaRow.parentNode.deleteRow(criteriaRow.rowIndex);
  }

  this.createAddCriteriaLink = function () {
    var criteriaAddAction = document.createElement("a");
    criteriaAddAction.href="javascript:void(0)";
    criteriaAddAction.onclick = getSearchTableAddLinkOnClickFunction(this);
    var icon = this.addIcon.cloneNode(true);
    criteriaAddAction.appendChild(icon);
    return criteriaAddAction;
  }

  this.createRemoveCriteriaLink = function(criteriaRow) {
    var criteriaRemoveAction = document.createElement("a");
    criteriaRemoveAction.href="javascript:void(0)";
    criteriaRemoveAction.onclick = getSearchTableRemoveLinkOnClickFunction(this, criteriaRow);
    var icon = this.deleteIcon.cloneNode(true);
    criteriaRemoveAction.appendChild(icon);

    return criteriaRemoveAction;
  }

}

/*
    SearchTable helper functions
*/

function getSearchTableAddLinkOnClickFunction(searchTable) {
  return function () {
    searchTable.addCriteriaRow();
  }
}

function getSearchTableRemoveLinkOnClickFunction(searchTable, criteriaRow) {
  return function () {
    searchTable.removeCriteriaRow(criteriaRow)
  }
}