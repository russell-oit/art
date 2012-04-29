var workCount = 0;
function artAddWork() {
   workCount++;
   
   //alert("WorkCount is: " + workCount);
   
   if (workCount > 0) {
      Element.show('systemWorking');
   }
   
   //alert("artAddWork " + workCount);

}
function artRemoveWork() {
   workCount--;
   
   //alert("WorkCount is: " + workCount);
   
   if (workCount <= 0) {
     Element.hide('systemWorking');
   } 
   //alert("artRemoveWork: " + workCount);
}


function startInNewWindow(page) {
OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,location=no,statusbar=no,scrollbars=yes,resizable=yes,width=800,height=600");
}

function btnup(btn) { btn.className='buttonup'; }

function btndn(btn) { btn.className='buttondn'; }

function openHelp(page) {
  OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,locationno,statusbar=no,scrollbars=yes,resizable=yes,width=400,height=600");
}

function writeStatus(message) {
  document.getElementById("statusDiv").innerHTML = message;
}

function writeInfo(message) {
  document.getElementById("infoDiv").innerHTML = message;
}

function Start(page) {
 OpenWinMenu = this.open(page, "", "toolbar=yes,menubar=yes,location=no,status=yes,statusbar=yes,scrollbars=yes,resizable=yes,width=800,height=600");
}

function validateTinyMCE(tinyAreaId, maxLen) {
     if(tinyMCE.get(tinyAreaId).getContent().length>=maxLen) {
          alert('Message Too Long! Reduce text or formatting options');
          return false;
     } else {
          return true;
     }
}

function showHide(item){
 if (item.className == "collapse") {
     item.className="expand";
 } else {
     item.className="collapse"; 
 }
}

function setClass(obj, cssName) {
     obj.className = cssName;
}
