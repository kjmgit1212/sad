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
 * JSP for doing self-registration.
 * 
 * NOTE:  The registerServlet does a request.getParameters() and uses
 * any parameters that were submitted to build up the Person object.
 * In this way, you can add input boxes to the jsp to collect more
 * information about the user and not have to change the servlet.
 * The downside to this convenience is that if you add any attributes
 * to the form that do not belong on the Person object, you will get
 * a SchemaViolationException from TIM when it tries to crate the Person.
 *
 *********************************************************************/
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<%@ page language="java" contentType="text/html; charset=UTF-8"%>

	<head>
		<meta name="GENERATOR" content="IBM WebSphere Studio"/>
		<title>Tivoli Identity Products Integration Sample - Register As A New User</title>
		<link rel=StyleSheet href="/itim_expi/css/imperative.css" media="screen" type="text/css"/>

	</head>
	<script language="JavaScript">

		function goToMainPage() {
   			document.location="/itim_expi/jsp/protected/main.jsp";
		}

		<%
			// The selfRegistration does not expect a logonID to be available so clear it now

			session.setAttribute("logonID","");
		%>

	</script>

	<%@ include file="expi_header.jspf" %>

		<table border=0 cellpadding=0 cellspacing=0 width=100%>
			<tr>
				<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
				<td width="100%" class="text-normal">

					<table border=0 cellpadding=0 cellspacing=0 width=90%>
						<tr>
							<td><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
						</tr>
						<tr>
							<td class="heading-text">Register as a New User</td>
						</tr>
						<tr>
							<td class="heading-line"><img src="/itim_expi/images/img_clear.gif" width="1" height="2"/></td>
						</tr>
						<tr>
							<td class="text-description">To register as a new user, please fill in all of the fields below.</td>
						</tr>
					</table>

					<!-- Conditionally display error messages -->
					<table>
						<tr>
							<td><%@ include file="error_msg_box.jsp" %></td>
						</tr>
					</table>

					<table border=0 cellpadding=0 cellspacing=0 width=100%>
						<tr>
							<td width="80"><img src="/itim_expi/images/img_clear.gif" width="80" height="10"/></td>
						</tr>
						<tr>
							<td>
								<form name="regForm" method="POST" action="/itim_expi/registerServlet" autocomplete="off">
									<table border="0" width="377" cellspacing="0" cellpadding="3">
										<tr>
											<td width="90%" nowrap class="text-normal">Full Name:<br> 
												<input name="cn" value='' size="40" class="entry-field"/>
											</td>
										</tr>

										<tr>
											<td width="90%" nowrap class="text-normal">First Name:<br> 
												<input name="givenname" value='' size="40" class="entry-field"/>
											</td>
										</tr>

										<tr>
											<td width="90%" nowrap class="text-normal">Last Name:<br> 
												<input name="sn" value='' size="40" class="entry-field"/>
											</td>
										</tr>

										<tr>
											<td width="90%" nowrap class="text-normal">Email Address:<br> 
												<input name="mail" value='' size="40" class="entry-field"/>
											</td>
										</tr>

										<tr>
											<td>&nbsp;</td>
										</tr>

									</table>

									<input type="submit" value="Register" class="button"
										onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')"/>

									<input type="button" name="Cancel" id="Cancel" value="Cancel" class="button" onMouseOver="pviiClassNew(this,'buttonover')" onMouseOut="pviiClassNew(this,'button')" onClick="goToMainPage()"/>

								</form>

							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</body>
</html>
