/**
 * 
 * Runs request and sets response
 * Function will fetch all user entered data and build Query String
 * Send POST request to servlet 
 * Read servlet Response 
 * Display Server response to user
 * 
 * Author: Jean-Pierre Erasmus
 */
function sendRequest() {
    var http = new XMLHttpRequest();
    var url = "CallRequestServlet";
    http.open("POST", url, true);

    //Paramaters to pass
    var svrname = document.getElementById("serverName").value;
    var portNo = document.getElementById("portNo").value;
    var evntpe = document.getElementById("eventType").options[document.getElementById("eventType").selectedIndex].value;
    var usrpin = document.getElementById("userPin").value;
    var dvcid = document.getElementById("deviceID").value;
    var serial = document.getElementById("deviceSerial").value;
    var version = document.getElementById("deviceVersion").value;
    var tranType = document.getElementById("tranType").options[document.getElementById("tranType").selectedIndex].value;

    //Build QueryString
    var qryString = "serverName=" + svrname + "&portNo=" + portNo + "&eventType=" + evntpe +
            "&userPin=" + usrpin + "&deviceID=" + dvcid + "&deviceSerial=" + serial + "&deviceVersion=" + version + "&tranType=" + tranType;

    //Send header content type
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    //Get Response back
    http.onreadystatechange = function () {
        if (http.readyState === 4 && http.status === 200) {
            var responseText = http.responseText;
            //Display Response Message in green LCD Block
            document.getElementById("responseMesg").innerHTML = responseText;
        }
    };

    //Send Request
    http.send(qryString);
}