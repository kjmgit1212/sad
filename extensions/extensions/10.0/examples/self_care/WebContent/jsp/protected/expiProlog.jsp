<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2007, 2009 All Rights Reserved
*  
*  US Government Users Restricted Rights - Use, duplication or
*  disclosure restricted by GSA ADP Schedule Contract with
*  IBM Corp.
********************************************************************/
-->
<!--
Source: expiProlog
		This module contains prologue code for all JSPs that require
		the user to be authenticated prior to allow them access to a page.
		If the user is NOT authenticated, the Subject will not exist in the 
		header.
-->
		
<%@ page import="javax.security.auth.Subject" %>
<%

	// check if the session is valid...set the error message
	// and redirect to the logon page
	HttpSession session1 = request.getSession(false);
    if (session1 == null) {
    	out.println("session1 = null");
        request.setAttribute("message", "The Session is no longer valid");
        response.sendRedirect("/itim_expi/jsp/unprotected/logon.jsp");
    }
    
	Subject subject = (Subject)session.getAttribute("subject");
	if (subject == null) {
		out.println("Session is not valid (no subject).");
		session.invalidate();
        request.setAttribute("message", "The Session is no longer valid.");
 		response.sendRedirect("/itim_expi/jsp/unprotected/logon.jsp");
	}    
%>
