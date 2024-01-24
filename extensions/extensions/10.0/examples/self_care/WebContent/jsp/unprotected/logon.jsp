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

<%@ page import="examples.expi.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<html>
	<head>
		<title>Sign On to the Tivoli Identity Products Integration Sample</title>

		<%
			boolean fromWebSEAL = false;
			String UID=null;

			String ssoEnabled = (String)request.getAttribute(ExpiUtil.SSO_ENABLED);

			if (ssoEnabled != null && ssoEnabled.equals("yes")) {
				String ivuser = request.getHeader("iv-user" );
				if (( ivuser != null ) && (ivuser.length() > 0 )) {
					fromWebSEAL = true;
					UID = ivuser.trim();
					System.out.println( "SSO: UID=" + UID );
				}
			}
		%>

		<script language="JavaScript">

			function getRegisterURL() {
				document.location= "/itim_expi/jsp/unprotected/selfregister.jsp";
			}

			function doLogin() {
				if (document.loginForm.j_username == null || document.loginForm.j_username.value.length > 0) {
					document.loginForm.action = document.location.protocol + "//" + document.location.host + "/itim_expi/j_security_check";
					return true;
				} else {
					alert( "Please supply your User ID and press the \"OK\" button.");
					return false;
				}
			}

			function forgotPassword() {
				if (document.loginForm.j_username == null || document.loginForm.j_username.value.length > 0) {
					document.loginForm.action = document.location.protocol + "//" + document.location.host + "/itim_expi/ForgotPasswordServlet";
					document.loginForm.submit();
				} else {
					alert( "Please enter your User ID and press the \"Forgot Your Password?\" link" );
				}
			}

		</script>
	</head>

	<%
		if (fromWebSEAL) {
	%>
				<body onLoad="doLogin()">
					<form name="loginForm" method="POST">
						<input name="j_username" type="hidden" size="20" class="entry-field" value="<%=UID%>"/>
						<input name="j_password" type="hidden" size="20" class="entry-field" value="none"/>
					</form>

				</body>
			</html>

	<%
		} else {
	%>

				<!-- header -->
				<%@ include file="expi_logon_header.jspf" %>

					<table width="100%" border="0" cellspacing="0" cellpadding="3">
						<tr>
							<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
							<td width="100%">
								<table width="90%" border="0" cellspacing="0" cellpadding="0">
									<tr>
										<td class="heading-text">Sign On</td>
									</tr>
									<tr>
										<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"/></td>
									</tr>

									<!-- Info message -->
									<tr>
										<td class="text-description">To sign on, please enter your user ID and password.</td>
									</tr>

									<!-- Display any error messages -->
									<tr>
										<td><%@ include file="error_msg_box.jsp" %></td>
									</tr>
								</table>

								<br>

								<!-- Login form -->
								<form name="loginForm" method="POST" action="/itim_expi/j_security_check">
									<table width="102%" border="0" cellspacing="0" cellpadding="3">
										<tr>
											<td width="90%" nowrap class="text-normal">User ID:<br>
												<input name="j_username" type="text" size="20" class="entry-field"/>
											</td>
										</tr>
										<tr>
											<td nowrap class="text-normal">Password:<br>
												<input name="j_password" type="password" size="20" class="entry-field"/>
											</td>
										</tr>
										<tr>
											<td class="text-normal">
												<input type="submit" name="ACTION" value="OK" class="button"
													onMouseOver="pviiClassNew(this,'buttonover')"
													onMouseOut="pviiClassNew(this,'button')"/>
											</td>
										</tr>
										<tr>
											<td class="text-normal"><br>
												<b><a href="/itim_expi/jsp/unprotected/selfchangepwd.jsp"><span class=pci-link>
												Change Your Password</span></a></b>
											</td>
										</tr>
										<tr>
											<td class="text-normal"><br>
												<b><a href="Javascript:forgotPassword();"><span class=pci-link>
												Forgot Your Password?</span></a></b>
											</td>
										</tr>
										<tr>
											<td class="text-normal"><br>
												<b><a href="Javascript:getRegisterURL();"><span class=pci-link>
												Register as a new user?</span></a></b>
											</td>
										</tr>

										<tr>
											<td><img src="/itim_expi/images/img_clear.gif" width="100%" height="10"/></td>
										</tr>

										<tr>
											<td><img src="/itim_expi/images/img_clear.gif" width="100%" height="10"/></td>
										</tr>

									</table>

									<!-- Build info -->
									<table width="90%" border="0" cellspacing="0" cellpadding="0>

										<tr>
											<td>
												<img src="/itim_expi/images/img_clear.gif" width="100%" height="10"/>
											</td>
										</tr>

										<tr>
											<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"/></td>
										</tr>

										<tr>
											<td class="text-description" align="right">&nbsp;</td>
										</tr>
									</table>

								</form>
							</td>
						</tr>

						<tr>
							<td><img src="/itim_expi/images/img_clear.gif" width="100%" height="10"/></td>
						</tr>

					</table>

				</body>
				<!-- Start tag is in expi_logon_header.jspf -->
			</html>

	<%
		}
	%>
