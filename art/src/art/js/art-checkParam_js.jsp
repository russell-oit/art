/*
 *  ART - JavaScript used to check user-fillable parameters
 * Date validation function from date.js
 */
<%
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>


  function ValidateValue(fieldDataType, fieldName, fieldValue){
     if (fieldDataType == "INTEGER") return ValidateInteger( fieldName, fieldValue);
     if (fieldDataType == "NUMBER" ) return ValidateNumber( fieldName, fieldValue);

	 if (fieldDataType == "DATE" ) return validateDate( fieldName, fieldValue);
	 if (fieldDataType == "DATETIME" ) return validateDateTime( fieldName, fieldValue);

  }

  function ValidateInteger( fieldName, numValue){
   if ( (numValue - parseInt(numValue)) == 0 ) {
      return true;
   }
   alert("\"" + fieldName +"\" <%=messages.getString("shouldBeInt")%>");
   return false;
  }

  function ValidateNumber( fieldName, numValue){
   if ( (numValue - parseFloat(numValue)) == 0 ) {
      return true;
   }
   alert("\"" + fieldName +"\" <%=messages.getString("shouldBeNum")%>");
   return false;
  }

function validateDate( fieldName, dateValue){
	if(dateValue==""){
		//now
		return true;
	} else if(dateValue.substring(0,3).toUpperCase()=="ADD"){
		//dynamic date
		return true;
	} else if(Date.isValid(dateValue,"yyyy-MM-dd")){
		//valid date
		return true;
	} else {
		alert("\"" + fieldName +"\" <%=messages.getString("shouldBeDate")%>");
		return false;
	}
 }

function validateDateTime( fieldName, dateValue){
	if(dateValue==""){
		//now
		return true;
	} else if(dateValue.substring(0,3).toUpperCase()=="ADD"){
		//dynamic date
		return true;
	} else if(Date.isValid(dateValue,"yyyy-MM-dd HH:mm") || Date.isValid(dateValue,"yyyy-MM-dd HH:mm:ss")){
		//valid datetime
		return true;
	} else {
		alert("\"" + fieldName +"\" <%=messages.getString("shouldBeDateTime")%>");
		return false;
	}
 }


  function returnTrue(){ // That's a useful one...
   return true;
  }


  function addElementToSelectItem(selId) {
     chainedElem = document.getElementById(selId);
     elType = chainedElem.type;
     //if (chainedElem.options.length > 0)
     if (elType.substring(0,6) == "select") {
	chainedElem.options[chainedElem.options.length] = new Option(":::", "");
        //chainedElem.selectedIndex = (chainedElem.options.length-1);
     } else {
        chainedElem.value = "";
     }
  }



