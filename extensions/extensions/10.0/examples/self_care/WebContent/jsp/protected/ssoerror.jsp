<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
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
<HTML>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<HEAD>
<META name="GENERATOR" content="IBM WebSphere Studio">
<TITLE>Tivoli Identity Products Integration Sample</TITLE>
</HEAD>

<%@ include file="../unprotected/expi_header.jspf" %>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td class="text-description" align="center"><NOBR> 
			</tr>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">SSO Error Information Page</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">The following error has occurred. 
				Please check the server logs for additional information. <br>
				</td>
			<!-- Display any error messages -->
			<tr>
				<td><%@ include file="../unprotected/error_msg_box.jsp" %></td>
			</tr>
			</tr>
		</table>

		</td>
	</tr>
</table>
</body>
</html>

