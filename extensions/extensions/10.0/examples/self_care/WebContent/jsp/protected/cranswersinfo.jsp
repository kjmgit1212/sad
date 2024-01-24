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
 * JSP to display informational messages after the user has
 * changed his challenge-response answers. 
 *
 *********************************************************************/
-->
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<html>
<head>
<title>Change Challenge/Response Answers</title>

<script language="JavaScript">
function goToMainPage() {
   document.location="/itim_expi/jsp/protected/main.jsp";
}
</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<%@ include file="../unprotected/expi_header.jspf" %>
<br>

<table width="100%" border="0" cellspacing="0" cellpadding="3">
  <tr> 
    <td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
    <td width="100%"> <table width="90%" border="0" cellspacing="0" cellpadding="0">
        <tr> 
          <td class="heading-text">Change Challenge/Response Answers</td>
        </tr>
        <tr> 
          <td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"></td>
        </tr>

        <!-- If there's an info message, display it! -->
        <tr> 
        <%
        String infoMessage = (String)session.getAttribute("infoMessage");
        if (infoMessage != null)
           out.println("<td class=\"text-description\">" + infoMessage + "</td>");
        session.setAttribute("infoMessage", null);
        %>
        </tr>

        <!-- If there's an error message, display it! -->
        <tr> 
           <td><%@ include file="../unprotected/error_msg_box.jsp" %></td>
        </tr>

        <tr> 
          <td><br>
            <input type="submit" name="OK" id="Cancel" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()"> 
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
