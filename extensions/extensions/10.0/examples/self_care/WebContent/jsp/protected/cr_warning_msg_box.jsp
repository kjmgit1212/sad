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
 * JSP that handles displaying a warning message about whether 
 * or not a user needs to update his challenge/response answers.
 * This page is included in main.jsp. 
 *
 *********************************************************************/
-->

   <%
   Boolean changeNeeded = (Boolean)session.getAttribute("updateCRAnswers");
   if ((changeNeeded != null) && (changeNeeded.booleanValue() == true)) {
   %>

   <br>
   <table align="center" border="0" cellpadding="0" cellspacing="1" class="table-border-message" dir="LTR">

      <tr align="left" valign="TOP">
         <td align="center" class="message-background" valign="top" width="50">
            <img src="/itim_expi/images/message_warning.gif">
         </td>

         <td align="left" valign="middle" width="*">
            <p><span class="text-normal"><b>Challenge/Response Answers require updating.</b>  Before signing out you should update your challenge/response answers.  You can do this by clicking on the "Change My Challenge/Response Answers" link below.  If you do not change your answers, you will not be able to use the "Forgot My Password" feature.</span> &nbsp;
            </p>
         </td>

      </tr>
   </table>

   <%
   // NOTE: Do not set changeNeeded to false here!  
   // ChangeChallengeResponseAnswersServlet takes care of that!
   }
   %>
   
