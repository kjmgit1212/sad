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
 * JSP for changing an expired password.  
 *
 * When doing a JAAS login to TIM, if the TIM account password
 * is expired a CredentialExpiredException is thrown.  The
 * logonServlet catches the exception and forwards the user here.
 *
 *********************************************************************/
-->
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<html>
<head>
<title>Password Expired - Change Password</title>

<script language="JavaScript">
function goToLoginPage() {
   document.location = "/itim_expi/jsp/unprotected/logon.jsp";
}
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
          <td class="heading-text">Change Password</td>
        </tr>

        <tr> 
          <td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"></td>
        </tr>

        <!-- Display info message -->
        <tr> 
           <td class="text-description">To change your password, please fill in all of the fields below and click OK.</td>
        </tr> 

        <!-- Display expired password warning message if neeed! -->
        <tr> 
           <td>
               <%@ include file="expired_pwd_warning_msg_box.jsp" %>
           </td>
        </tr>
        

        <!-- If there's an error message, display it! -->
        <tr>
           <td><%@ include file="error_msg_box.jsp" %></td>
        </tr>

      </table>


    <!-- change password data -->
    <form action="/itim_expi/ChangePasswordServlet" method="POST">
    <input type="hidden" name="action" value="useSelfPasswordManager">

      <br> 
      <table width="102%" border="0" cellspacing="0" cellpadding="3">
        <tr> 
          <%
             // Prevent the ID from showing up as "null"
             String logonID = (String)session.getAttribute("logonID");
             if (logonID == null) 
                logonID = new String("");
          %>
          <td width="90%" nowrap class="text-normal">User ID<br> <input name="logonID" type="text" size="20" class="entry-field" value="<%= logonID %>" > 
          </td>
        </tr>
        <tr> 
          <td width="90%" nowrap class="text-normal">Current Password<br> <input name="currentPassword" type="password" size="20" class="entry-field"> 
          </td>
        </tr>
        <tr> 
          <td nowrap class="text-normal">New Password<br> <input name="newPassword" type="password" size="20" class="entry-field"> 
          </td>
        </tr>
        <tr> 
          <td class="text-normal">Confirm New Password<br> <input name="confirmNewPassword" type="password" size="20" class="entry-field"></td>
        </tr>
        <tr> 
          <td class="text-normal"><br><input type="submit" name="OK" id="OK" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')"> 
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
