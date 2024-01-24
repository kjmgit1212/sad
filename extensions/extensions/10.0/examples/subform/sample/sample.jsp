<!--
/********************************************************************
*  Licensed Materials - Property of IBM
*  
*  (c) Copyright IBM Corp.  2007 All Rights Reserved
*  
*  US Government Users Restricted Rights - Use, duplication or
*  disclosure restricted by GSA ADP Schedule Contract with
*  IBM Corp.
********************************************************************/
-->
<%@ page language="java" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="sample_utils.jspf" %>
<html>
	<head>
		<%=printStatusInfo(request)%>
		<%=printContactInfo(request)%>
		<title>Emergency Contact Information</title>
		<script type="text/javascript" src="sample.js"></script>
		<link rel="stylesheet" type="text/css" href="sample.css"></link>
	</head>
	<body onload="preparePage();">
		<form action="" method="post" >
			<div class="title"><%=getUserName(request)%>'s Emergency Contact</div><hr/>
			<div class="instructions">Please provide the name and telephone number of a person that we may contact in case of an emergency.</div>
			<div class="entry">Name</div>
			<input type="text" name="name">
			<div class="entry">Telephone Number</div>
			<input type="text" name="telephone">
			<div class="entry">Relationship to you</div>
			<input type="text" name="relationship">
			<div id="readwrite_buttons" class="hide">
				<button id="ok" onclick="document.forms[0].submit();return false;">OK</button>
				<button id="cancel" onclick="javascript:window.close();return false;">Cancel</button>
			</div>
			<div id="readonly_buttons" class="hide">
				<button id="back" onclick="javascript:window.close();return false;">Back</button>
			</div>
		</form>
	</body>
</html>