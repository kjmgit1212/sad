<head>
 <meta http-equiv="CACHE-CONTROL" content="no-cache">
 <script lang="javascript">
 
 	function init(){
 		disableDivs();
 		var wsheaderRadio = document.getElementById("wsheader");
 		wsheaderRadio.checked="true";
 		document.getElementById("wscalltype").value="wsheader";
 		toggleDivs();
 		
 		<%
			Cookie[] requestCookies = request.getCookies();
			String hasLtpaCookie = "false";
			if(requestCookies != null) {
				for(Cookie cookie : requestCookies){
					if(cookie.getName().contains("LtpaToken2")){
     					hasLtpaCookie = "true";
  					}
				}
			}
		%>
 		
 		if (! <%=hasLtpaCookie%>){
 			document.location.reload(true);
 		}
 	}
 	
 	function getCookie(c_name)
	{
	  var c_value = document.cookie;
	  var c_start = c_value.indexOf(" " + c_name + "=");
	  if (c_start == -1)
	  {
	     c_start = c_value.indexOf(c_name + "=");
	  }
	  if (c_start == -1)
	  {
	     c_value = null;
	  }
	  else
	  {
	     c_start = c_value.indexOf("=", c_start) + 1;
	     var c_end = c_value.indexOf(";", c_start);
	     if (c_end == -1)
	     {
	        c_end = c_value.length;
	     }
	     c_value = unescape(c_value.substring(c_start,c_end));
	  }
	  return c_value;
	}

 	function toggleDivs(){
 		var selectedWSCallType = getSelectedWSCallType();
 		var endpointDiv = document.getElementById("wsEndpointDetails");
 		
 		if(selectedWSCallType.id == "wsheader"){
 			endpointDiv.style.display = "";
			document.getElementById("wscalltype").value="wsheader";
 		}
 	}
 	
 	function getSelectedWSCallType(){
 		var wscallTypeRadios = document.getElementsByName("wscallType");
 		for (var i = 0; i < wscallTypeRadios.length; i++) {       
	        if (wscallTypeRadios[i].checked) {
	        	return wscallTypeRadios[i];
	        }
		}
 	}
 	
 	function disableDivs(){
 		var endpointDiv = document.getElementById("wsEndpointDetails");
 		endpointDiv.style.display = "none";
 	}
 	
 	function logout(){
 		document.wsclientform.action='ibm_security_logout';
 		var logoutExitPage = document.getElementById('logoutExitPage');
 		logoutExitPage.value = '/jsp/Home.jsp';
 		document.wsclientform.submit();
 	}
 	
 	function callWS(){
 		document.wsclientform.action='${pageContext.request.contextPath}/WebCallServlet';
 		document.wsclientform.submit();
 	}
 </script>
</head>
<body onload="init()">
<font size='6' color='blue'>WebService Call</font>
<input type="button" value="Logout" onclick="logout()" style="float: right"/>

<hr>
<% 
	Cookie[] cookies=request.getCookies();
	if (cookies!=null) {
		Cookie cookie = null;
		for(int i=0;i<cookies.length;i++){
  			if(cookies[i].getName().contains("Ltpa")){
     			cookie = cookies[i];
  				break;
  			}
		}
	
		if(cookie != null){
			request.getSession().setAttribute("Ltpa",cookie.getValue());
		}else{
			response.sendRedirect("Home.jsp");
		}
	}else{
		response.sendRedirect("Home.jsp");
	}

String servletUri=request.getContextPath()+"/WebCallServlet";
%>

</span>
	<div id="stylized" style="height: 152px">
		<form  name="wsclientform" action=$action method="POST">
		   <input type="hidden" id="logoutExitPage" value="" name="logoutExitPage" />
		   <div>
		   		<label for="wscallType">Select the web service call type.</label>
		   		<br/>
		   		<div>
					<input type="radio" id="wsheader" name="wscallType" value="wsheader" onchange="toggleDivs()"/>WS-Security Header
					<input type="hidden" id="wscalltype" name="wscalltype"/>
				</div>
				<div id="wsEndpointDetails">
					<p>Specify the server host and port on which the ISIM web services are deployed.</p>
					<table>
						<tr>
							<td>
								<label for="isimWSHost">WS Host:</label>
							</td>
							<td>
								<input type="text" id="isimWSHost" name="isimWSHost" value="" />		
							</td>
						</tr>
						<tr>
							<td>
								<label for="isimWSPort">WS Port:&nbsp;</label>
							</td>
							<td>
								<input type="text" id="isimWSPort" name="isimWSPort" value="" />
							</td>
						</tr>
					</table>
				</div>
			</div>
		    <table>
		    	<tr>
		    		<td>
		    			<input TYPE="button" NAME="usersubmit"	VALUE="Get Principal User" onclick="callWS()">
		    		</td>
		    	</tr>
		    </table>
		</form>
	</div>
</body>