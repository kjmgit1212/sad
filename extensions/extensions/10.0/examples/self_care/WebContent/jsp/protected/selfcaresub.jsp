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
<TITLE>Tivoli Identity Products Integration Sample - Self-Care Submitted</TITLE>
</HEAD>

<%@ include file="../unprotected/expi_header.jspf" %>

<script language="JavaScript">
function goToMainPage() {
   document.location="/itim_expi/jsp/protected/main.jsp";
}
</script>


<table border=0 cellpadding=0 cellspacing=0 width=90%>
	<tr>
		<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
		<td width="100%">

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"></td>
			</tr>
			<tr>
				<td class="heading-text">Change My Personal Information</td>
			</tr>
			<tr>
				<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1"
					height="2"></td>
			</tr>
			<tr>
				<td class="text-description">Your changes have been
				successfully submitted.</td>
			</tr>
		</table>

		<P>
		<table>

        <tr>
          <td><br>
            <input type="button" name="OK" id="Cancel" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()">
          </td>
        </tr>
		</table>
		</td>
	</tr>
</table>
</body>
</html>

