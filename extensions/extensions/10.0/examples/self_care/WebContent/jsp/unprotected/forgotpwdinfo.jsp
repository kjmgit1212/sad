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
<!--
/*********************************************************************
 * FILE: %Z%%M%  %I%  %W% %G% %U%
 *
 * Description:
 *
 * JSP for displaying results of a user attempting to answer his
 * challenge/response questions.
 *********************************************************************/
-->
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

        <tr> 
           <td class=text-description>You have successfully answered the challenges.  A password change request has been submitted to the server.</td>
        </tr> 

        <!-- If there's an info message, display it! -->
        <tr> 
        <%
        String infoMessage = (String)session.getAttribute("infoMessage");
        if ( (infoMessage != null) && (! infoMessage.equals("")))
           out.println("<td class=\"text-normal\"><br>" + infoMessage + "</td>");
        session.setAttribute("infoMessage", null);
        %>
        </tr>

        <!-- If there's an error message, display it! -->
        <tr> 
           <td><%@ include file="error_msg_box.jsp" %></td>
        </tr>

        <tr> 
          <td><br>
            <input type="submit" name="OK" id="Cancel" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToLoginPage()"> 
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
