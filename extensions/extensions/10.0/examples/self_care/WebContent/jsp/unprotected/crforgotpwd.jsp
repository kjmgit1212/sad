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
 * JSP to display the challenges and prompt for responses.
 * All logic is handled by ForgotPasswordServlet.
 *********************************************************************/
-->

<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<head>
<title>Tivoli Identity Products Integration Sample</title>
<link rel=StyleSheet HREF="/itim_expi/css/imperative.css" media="screen" type="text/css">
</head>

<%@ include file="expi_header.jspf" %>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td class="text-description" align="center"><NOBR> <A
					href="logon.jsp">Logon</A>&nbsp;&nbsp; 
			</tr>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Challenge And Response - I forgot my
				password</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">Please enter the following data items.</td>
			</tr>
		</table>

		<table>
			<!-- CODE GOES HERE -->
		</table>
		<td>
	</tr>
</table>
</body>
</html>

