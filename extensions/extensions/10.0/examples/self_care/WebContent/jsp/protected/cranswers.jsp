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
 * JSP for a user to set their challenge-response answers.
 * NOTE: The sample application has support for ADMIN-DEFINED
 * challenge definition mode only.  The user must provide an
 * answer to each challenge.  All logic is handled by the
 * ChangeChallengeResponseServlet.
 *
 *********************************************************************/
-->

<%@ page import="java.util.*" %>
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
      <td width="80">
         <img src="/itim_expi/images/img_clear.gif" width="80" height="10">
      </td>

      <td width="100%">
         <table width="90%" border="0" cellspacing="0" cellpadding="0">
            <tr>
               <td class="heading-text">Change Challenge/Response Answers</td>
            </tr>

            <tr>
               <td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"></td>
            </tr>

            <!-- Description -->
            <tr>
               <td class="text-description">
                  Change your answers and click OK.
                  You must provide an answer to each challenge.<br>
              </td>
           </tr>
         </table>
         <br>

         <!-- Informational message -->
         <table width="90%" border="0" cellpadding="0" cellspacing="1" class="table-border-message" dir="LTR">
            <tr align="left" valign="TOP">
               <td align="center" class="message-background" valign="top" width="50">
                  <img src="/itim_expi/images/message_information.gif">
               </td>

               <td align="left" valign="middle" width="*" wrap>
                  <p>
                     <span class="text-normal">When you click on the "Forgot My Password?" link on the Sign On page you must type the answers exactly as you specify them here.</span> &nbsp;
                  </p>
               </td>

            </tr>

         </table>

         <!-- If there's an error message, display it! -->
         <table width="90%" border="0" cellspacing="0" cellpadding="0">
            <tr>
               <td><%@ include file="../unprotected/error_msg_box.jsp" %></td>
            </tr>
         </table>

         <br>
         <!-- Challenge/response answers -->
         <form action="/itim_expi/ChangeChallengeResponseServlet" method="POST">
         <input type="hidden" name="action" value="setAnswers">

         <table width="100%" border="0" cellspacing="0" cellpadding="3">
         <%
          // Get the session so that it has challengesAndResponses initialized
          session = request.getSession(false);
          Map cars = (Map)session.getAttribute("challengesAndResponses");
          if (cars != null) {
             Iterator it = cars.keySet().iterator();
             while (it.hasNext()) {
                String challenge = (String)it.next();
                String answer = (String)cars.get(challenge);
          %>

          <tr>
             <td nowrap class="text-normal"><%= challenge %><br></td>
          </tr>

          <tr>
             <td width="35%" nowrap class="text-normal">Answer:<br>
                <input name="<%= challenge + "Answer" %>"
                       value="<%= answer %>"
                       type="password"
                       size="25"
                       class="entry-field">
             </td>

             <td width="65%" nowrap class="text-normal">Confirm Answer:<br>
                <input name="<%= challenge + "ConfirmAnswer" %>"
                       value="<%= answer %>"
                       type="password"
                       size="25"
                       class="entry-field">
             </td>
          </tr>

          <tr>
             <td nowrap class="text-normal"><br></td>
          </tr>

       <%
          }
       }
       %>

       <!-- buttons -->
       <tr>
          <td>
             <input type="submit" name="OK" id="OK" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')">
             <input type="button" name="Cancel" id="Cancel" value="Cancel" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()">
          </td>
       </tr>
       </table>

      </td>
   </tr>
</table>

</body>
</html>

