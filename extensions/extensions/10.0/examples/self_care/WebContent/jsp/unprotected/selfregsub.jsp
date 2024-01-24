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
 * JSP for displaying info about submitted self-registration requests.
 *
 *********************************************************************/
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<HEAD>
<META name="GENERATOR" content="IBM WebSphere Studio">
<TITLE>Tivoli Identity Products Integration Sample - Register as a New User</TITLE>
</HEAD>

<script language="JavaScript">
function goToLoginPage() {
   document.location = "/itim_expi/jsp/unprotected/logon.jsp";
}
</script>


<%@ include file="expi_header.jspf" %>

<table border=0 cellpadding=0 cellspacing=0 width=100%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">

		<table border=0 cellpadding=0 cellspacing=0 width=90%>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Register as a New User</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">Your registration request was successfully submitted. <br>
				</td>
			</tr>

			<tr>
				<td class="text-normal"><br> Please wait for an email confirmation and contact information before accessing the site's features.</td>
			</tr>

			<tr>
				<td class="text-normal"><br>
						<input type="button" name="OK" id="OK" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToLoginPage()">
				</td>
			</tr>

		</table>
		</td>
	</tr>


</table>
</body>
</html>

