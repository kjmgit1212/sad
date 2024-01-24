<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2003, 2009 All Rights Reserved
*  
*  US Government Users Restricted Rights - Use, duplication or
*  disclosure restricted by GSA ADP Schedule Contract with
*  IBM Corp.
********************************************************************/
-->
<%@ page import="java.util.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<html>
<head>
<title>Forgot Password</title>
<script language="JavaScript">

function goToLoginPage() {
   document.location = "/itim_expi/jsp/unprotected/logon.jsp";
}

<!--
function pviiClassNew(obj, new_style) { //v2.6 by PVII
  obj.className=new_style;
}
//-->

</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<%@ include file="expi_logon_header.jspf" %>
<br>

<table width="100%" border="0" cellspacing="0" cellpadding="3">
  <tr> 
    <td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
    <td width="100%"> <table width="90%" border="0" cellspacing="0" cellpadding="0">
        <tr> 
          <td class="heading-text">Forgot Password</td>
        </tr>
        <tr> 
          <td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"></td>
        </tr>

        <!-- Informational message -->
        <tr> 
           <td class="text-description">To reset your password, respond to each of the challenges below and click OK.</td>
        </tr>
 
        <!-- If there's an error message, display it! -->
        <tr> 
           <td><%@ include file="error_msg_box.jsp" %></td>
        </tr>
      </table>


    <!-- forgot password data -->
    <form action="/itim_expi/ForgotPasswordServlet" method="POST">
    <input type="hidden" name="action" value="authenticate">

      <br> 

      <table width="102%" border="0" cellspacing="0" cellpadding="3">

      <!-- Display the challenges -->

      <%
       Map challenges = (Map)session.getAttribute("challenges");
       if (challenges != null) {
          Iterator it = challenges.keySet().iterator();
          while (it.hasNext()) {
             String challenge = (String)it.next();
       %>

       <tr>
          <td nowrap class="text-normal"><%= challenge %><br><input name="<%= challenge %>" type="password" size="20" class="entry-field">

       </tr>
 
       <%
          }
       }
       %>

        <!-- buttons -->

        <tr> 
          <td><br><input type="submit" name="OK" id="OK" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')"> 
            <input type="button" name="Cancel" id="Cancel" value="Cancel" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToLoginPage()"> 
          </td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
      </table></td>
  </tr>
</table>
</body>
</html>
