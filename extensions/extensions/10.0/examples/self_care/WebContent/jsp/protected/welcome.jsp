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
<%
HttpSession session1 = request.getSession();
Object dest_url = session1.getAttribute("dest_url");
if(dest_url!=null && dest_url.toString().length()>0){
session1.removeAttribute("dest_url");
}
%>
<META name="GENERATOR" content="IBM WebSphere Studio">
<TITLE>Tivoli Identity Products Integration Sample</TITLE>
<LINK REL=StyleSheet HREF="/itim_expi/css/imperative.css" media="screen"
	type="text/css">
</HEAD>

<%@ include file="../unprotected/expi_header.jspf" %>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td class="text-description" align="center"><NOBR> <A
					href="jsp/logon.jsp">Logon</A>&nbsp;&nbsp; <A href="jsp/selfregister.jsp">Register</A>&nbsp;&nbsp;
				
			</tr>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Welcome</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">Welcome to the Tivoli Identity Product
				Integration Sample</td>
			</tr>
		</table>
		</td>
	</tr>
 <!-- 
 Optionally include a footer.  This could contain Copyrights or other generic
 information that is to be shared by all of the JSP's.
 
	<tr>
		<td><img src="/itim_expi/images/img_clear.gif" width="100%" height="30"></td>
		<td width="100%"><%@ include file="../../expi_footer.jspf" %></td>
	</tr>
 -->
</table>
</body>
</html>

