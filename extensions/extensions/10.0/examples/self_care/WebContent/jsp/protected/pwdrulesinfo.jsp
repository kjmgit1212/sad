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
 * This JSP handles the displaying of a user's password rules.
 * Used by ChangePasswordServlet when the user tries to change
 * their password to one that is invalid.
 *********************************************************************/
-->

<%@ page import="java.util.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<html>
<head>
<title>Change Password</title>

<script language="JavaScript">
function goToChangePasswordPage() {
   document.location="/itim_expi/jsp/protected/changepwd.jsp";
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
          <td class="heading-text">Change Password</td>
        </tr>
        <tr> 
          <td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"></td>
        </tr>

        <!-- Display error message -->
        <tr> 
           <td><%@ include file="../unprotected/error_msg_box.jsp" %></td>
        </tr>

        <tr> 
           <td class="text-normal"> 
              <br>Here are the password rules that apply to your accounts: <br>
           </td> 
        </tr>

        <tr><td class="text-normal"> 
           <table border="1" color="#435E88" width="85%" cellspacing="0" cellpadding="3">
              <%
              Map rules = (Map)session.getAttribute("passwordRules");
              Iterator it = rules.keySet().iterator();
              while (it.hasNext()) {
                 String rule = (String)it.next();
                 String value = (String)rules.get(rule);
              %>
    
              <tr> 
                 <td class="text-normal"><%=rule%></td>
                 <td class="text-normal" align="center"><%=value%></td>
              </tr> 

              <%
              }
              session.setAttribute("rules", null);
              %>

           </table>
        </td></tr>

        <tr> 
          <td><br>
            <input type="submit" name="OK" id="Cancel" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToChangePasswordPage()"> 
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
