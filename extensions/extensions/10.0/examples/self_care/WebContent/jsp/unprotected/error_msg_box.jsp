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
 * JSP that handles displaying an error message in a box.
 * This page is included in other pages.
 *
 *********************************************************************/
-->

   <%
   String errorMessage = (String)session.getAttribute("errorMessage");
   if ((errorMessage != null) && (! errorMessage.equals(""))) {
   %>

      <br>
      <table align="center" border="0" cellpadding="0" cellspacing="1" class="table-border-message" dir="LTR">
         <tr align="left" valign="TOP">
            <td align="center" class="message-background" valign="top" width="50">
               <img src="/itim_expi/images/message_error.gif">
            </td>

            <td align="left" valign="middle" width="*">
               <p> 
                  <span class="text-normal"> <%= errorMessage %></span> &nbsp;
               </p>
            </td>

         </tr>
      </table>

   <%
   session.setAttribute("errorMessage", null);
   }
   %>
   
