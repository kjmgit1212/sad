<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2010 All Rights Reserved
*  
*  US Government Users Restricted Rights - Use, duplication or
*  disclosure restricted by GSA ADP Schedule Contract with
*  IBM Corp.
********************************************************************/
-->

<%@ page import="examples.expi.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<%
	String loginErrorMsg = null;
	Cookie [] cookies = request.getCookies(); 
	for(int i = 0; i < cookies.length; i++) {
	    Cookie c = cookies[i];
	    if(ExpiUtil.EXPI_LOGIN_ERROR.equals(c.getName())) {
	        loginErrorMsg = c.getValue();
	    }
	}
	session.setAttribute(ExpiUtil.ERR_LABEL, loginErrorMsg);
	String appContextRoot = request.getContextPath();
%>
<HTML>
	<BODY>
		<form id="form" method="POST" action="<%= appContextRoot %>/mainServlet">
		</form>
		<script language="javascript">
			var pageForm = document.forms[0];
			pageForm.submit();		
		</script>
	</BODY>
</HTML>