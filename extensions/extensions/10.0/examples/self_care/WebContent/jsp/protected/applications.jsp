<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2003, 2010 All Rights Reserved
*  
*  US Government Users Restricted Rights - Use, duplication or
*  disclosure restricted by GSA ADP Schedule Contract with
*  IBM Corp.
********************************************************************/
-->
<html>
	<%@ page language="java" contentType="text/html; charset=UTF-8"%>
	<%@ include file="expiProlog.jsp" %>

	<%@ page import="examples.expi.*" %>
	<%@ page import="java.util.*" %>
	<%@ page import="com.ibm.itim.common.*"%>

	<head>
		<meta name="GENERATOR" content="IBM WebSphere Studio"/>
		<title>Tivoli Identity Products Integration Sample</title>
		<script language="JavaScript">
			function getLogoutURL() {
				document.location= "./logon";
			}

			function getHomeURL() {
				document.location= "./logon";
			}

			function getChangePwdURL() {
				document.location= "./changepwd.jsp";
			}

			function submitApplications() {
				document.appForm.action = "./applicationServlet";
				document.appForm.submit();
			}

			function goToMainPage() {
				document.location="/itim_expi/jsp/protected/main.jsp";
			}
		</script>
	</head>

	<%@ include file="../unprotected/expi_header.jspf" %>

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
				<td width="100%">

					<table border=0 cellpadding=0 cellspacing=0 width=100%>
						<tr>
							<td class="heading-text">Applications Subscription</td>
						</tr>
						<tr>
							<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"/></td>
						</tr>
						<tr>
							<td class="text-description">Please make selections regarding the applications
							to which you would like to be subscribed, and click OK when done.</td>
						</tr>
					</table>

					<form name="appForm" method="POST" autocomplete="off">
						<table border="0" width="377">
							<tbody>

								<%			
									ExpiUtil utilObject = null;

									try {
										utilObject = new ExpiUtil();
									} catch (Exception e) {
										e.printStackTrace();
									}

									String appAttrs = utilObject.getProperty(ExpiUtil.APP_LIST);

									if (appAttrs != null) {
										StringTokenizer st = new StringTokenizer(appAttrs, ",");
										while (st.hasMoreTokens()) {
											String sAttr = st.nextToken();
											String isChecked = null;

											String sPropName = "application." + sAttr + ".name";
											String sPropNameDN = "application." + sAttr + ".dn";

											// get the attribute name and value from the properties file

											String sAttrText = utilObject.getProperty(sPropName);
											if (sAttrText == null) {
												sAttrText = "(Application name not defined in properties file)";
											}

											String sAttrValueDN = utilObject.getProperty(sPropNameDN);
											if (sAttrValueDN == null) {
												sAttrValueDN = "(Application group name not defined in properties file)";
											}

											// if this attribute was on the request header - that means it's selected
											// otherwise not.

											String sAttrValue = (String)request.getAttribute(sAttr);
											if (sAttrValue == null || sAttrValue.equals("")) {
												isChecked = "";
											} else {
												isChecked = "checked";
											}
								%>

											<tr>
												<th><input type="checkbox" name="<%= sAttr %>" value="<%= sAttrValueDN %>" <%= isChecked %>/></th>
												<td><%= sAttrText %></td>
											</tr>
								<%
										}
									}
								%>
							</tbody>
						</table>

						<br>
						<input type="submit" name="OK" id="OK" value="OK" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')"/>
						<input type="button" name="Cancel" id="Cancel" value="Cancel" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()"/>
					</form>
				</td>

			</tr>
		</table>
	</body>
</html>
