<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2007, 2009 All Rights Reserved
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
 * JSP that handles displaying a warning message about an 
 * expired password.  This page is included in selfchangepwd.jsp. 
 *
 *********************************************************************/
-->

   <%
   Boolean passwordExpired = (Boolean)session.getAttribute("passwordExpired");
   if ((passwordExpired != null) && (passwordExpired.booleanValue() == true)) {
   %>

   <br>
   <table align="center" border="0" cellpadding="0" cellspacing="1" class="table-border-message" dir="LTR">

      <tr align="left" valign="TOP">
         <td align="center" class="message-background" valign="top" width="50">
            <img src="/itim_expi/images/message_warning.gif">
         </td>

         <td align="left" valign="middle" width="*">
            <p><span class="text-normal"><b>Your password has expired.</b>                     You must change your password and then sign on with the new 
               password before you can continue.</span> &nbsp;
            </p>
         </td>

      </tr>
   </table>

   <%
   // NOTE: Do not set passwordExpired to false here!  
   // ChangePasswordServlet takes care of that!
   }
   %>
   
