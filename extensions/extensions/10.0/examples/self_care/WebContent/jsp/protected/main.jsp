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
 * JSP that handles the "main" or "home" page.
 * This is the page the user is forwarded to after a successful login.
 *
 *********************************************************************/
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html;CHARSET=UTF-8" >
<link href="/itim_expi/css/imperative.css" rel="stylesheet" type="text/css">
<title>Tivoli Identity Products Integration Sample - Home Page</title>
<%@ page import="examples.expi.*" %>
<%@ include file="expiProlog.jsp" %>

</HEAD>

<%@ include file="../unprotected/expi_header.jspf" %>

<table border=0 cellpadding=3 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">
		<table border=0 cellpadding=0 cellspacing=0 width=90%>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Home</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">Welcome!<br></td>
			</tr>

         <!-- Display any errors -->
         <tr>
            <td>
              <%@ include file="../unprotected/error_msg_box.jsp" %>
            </td>
         </tr>


         <!-- Warn about needed updates to challenge/response answers -->
         <tr>
            <td>
              <%@ include file="cr_warning_msg_box.jsp" %>
            </td>
         </tr>

			<tr>
				<td class="text-normal"><br>
              <ul>
                  <li><A href="/itim_expi/selfCareServlet">Change My Personal Information</A>
                  <li><A href="/itim_expi/jsp/protected/changepwd.jsp">Change My Password</A>

                  <li><A href="/itim_expi/ChangeChallengeResponseServlet">
                         Change My Challenge/Response Answers</A>

                  <!-- If TAM is installed, put up subscribe link -->
                  <%
					String sTAMConfigured = (String)session.getAttribute(ExpiUtil.TAM_SERVICE_ACTIVE);
				  	if (sTAMConfigured != null && sTAMConfigured.equalsIgnoreCase("true")) {
				  		%>
                  		<li><A href="applicationServlet">Subscribe to Applications</A>
                  		<%
				  	}
				  	
                  %>
              </ul>
				</td>
			</tr>

		</table>
		</td>
	</tr>

</table>


</body>
</html>

