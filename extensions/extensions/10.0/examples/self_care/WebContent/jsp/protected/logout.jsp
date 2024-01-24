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
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<HEAD>

<%@ page import="examples.expi.*" %>
<%@ page import="com.ibm.itim.apps.PlatformContext" %>

<% response.setHeader("Cache-Control", "no-cache"); 
   response.setHeader("Pragma", "no-cache"); %>
<%

/*
 * Invalidate the session, but first get the value of sso so 
 * it can be used later!
 */
String sso = (String)session.getAttribute(ExpiUtil.SSO_ENABLED);
PlatformContext platform=(PlatformContext) session.getAttribute(ExpiUtil.PLATFORM_CONTEXT);
HttpSession session1 = request.getSession(false);

if(platform != null) {
	platform.close();
	platform = null;
}

if (session1 != null) {
	session1.invalidate();
}

/* 
 * If sso is enabled, redirect to pkmslogout so the WebSeal credentials
 * will get cleared out!  Note that the rest of this page is not
 * displayed.  pkmslogout should be set so that it goes to a page
 * with an appropriate message.
 */
if (sso != null)
	response.sendRedirect("../../pkmslogout");

%>


<META name="GENERATOR" content="IBM WebSphere Studio">
</HTML>
<TITLE>Tivoli Identity Products Integration Sample</TITLE>
<LINK REL=StyleSheet HREF="/itim_expi/css/imperative.css" media="screen"
	type="text/css">
<SCRIPT language="JavaScript">

function goToLoginPage() {
   document.location="/itim_expi/ibm_security_logout?logoutExitPage=/mainServlet";
}

</SCRIPT>
</HEAD>

<%@ include file="../unprotected/expi_logon_header.jspf" %>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">
		<table border=0 cellpadding=0 cellspacing=0 width=90%>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Sign Off</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">You have been signed off.  To sign on, click the Sign On button.<br>
			</tr>

        <tr>
           <td class="text-normal">
            <br><input type="submit" name="Sign On" id="Sign On" value="Sign On" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToLoginPage()">
          </td>
        </tr>


		</table>
		</td>
	</tr>
</table>

</body>
</html>

