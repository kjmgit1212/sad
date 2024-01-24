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
 * JSP for handling self care.
 *********************************************************************/
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<%@ page language="java" contentType="text/html; charset=UTF-8"%>
	<%@ include file="expiProlog.jsp" %>

	<%@ page import="examples.expi.*" %>
	<%@ page import="java.util.*" %>
	<%@ page import="com.ibm.itim.common.*"%>

	<head>
		<meta name="GENERATOR" content="IBM WebSphere Studio"/>
		<title>Tivoli Identity Products Integration Sample - Change My Personal Information</title>
		<script language="JavaScript">

			function goToMainPage() {
				document.location="/itim_expi/jsp/protected/main.jsp";
			}

			function pviiClassNew(obj, new_style) { //v2.6 by PVII
				obj.className=new_style;
			}

		</script>
	</head>

	<%@ include file="../unprotected/expi_header.jspf" %>

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
				<td width="100%">

					<table border=0 cellpadding=0 cellspacing=0 width=90%>
						<tr>
							<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
						</tr>
						<tr>
							<td class="heading-text">Change My Personal Information</td>
						</tr>
						<tr>
							<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"/></td>
						</tr>
						<tr>
							<td class="text-description">Change your personal information and press OK when finished.</td>
						</tr>
					</table>

					<!-- Conditionally display error messages -->
					<table width="90%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><%@ include file="../unprotected/error_msg_box.jsp" %></td>
						</tr>
					</table>

					<!--main table-->
					<form name="selfCareForm" method="POST" autocomplete="off">
						<table border=0 cellpadding=0 cellspacing=0 width="100%" valign="top">
							<tr valign="middle">
								<td valign="top" colspan="2" class="text-normal">

									<form name="regForm" method="POST" autocomplete="off">
										<table border="0" width="377" cellspacing="0" cellpadding="3">
											<tbody>

												<%			
													ExpiUtil utilObject = null;

													try {
														utilObject = new ExpiUtil();
													} catch (Exception e) {
														e.printStackTrace();
													}

													String selfcareAttrs = utilObject.getProperty(ExpiUtil.SELFCARE_ATTRS);
													//System.out.println("selfcare attrs: " + selfcareAttrs);

													if (selfcareAttrs != null) {
														StringTokenizer st = new StringTokenizer(selfcareAttrs, ",");
														while (st.hasMoreTokens()) {
															String sAttr = st.nextToken();
															//System.out.println("   Attr: " + sAttr);

			 												// build the attribute name (replacing spaces with '.'

															String sAttr2 = "attributes." + sAttr;

															// get the attribute from the properties file

															String sAttrText = utilObject.getProperty(sAttr2);
															if (sAttrText == null) {
																sAttrText = "(no text defined)";
															}

															String sAttrValue = (String)request.getAttribute(sAttr);
															if (sAttrValue == null) {
																sAttrValue = "";
															}
												%>

															<tr>
																<td class="text-normal">
																	<%= sAttrText %>:<br>
																	<input name="<%= sAttr %>" class="entry-field"
																		value="<%= sAttrValue %>" type="text" size="40"/>
																</td>
															</tr>

												<%
														}
													}
												%>
											</tbody>
										</table>
										<br>
										<input type="submit" value="OK" class="button"
											onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')">
										<input type="button" name="Submit22" id="Submit22" value="Cancel"
											class="button" onMouseOver="pviiClassNew(this,'buttonover')"
											onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()">
									</form>
								</td>
							</tr>
						</table>
					</form>
				</td>
			</tr>
		</table>
	</body>
</html>
