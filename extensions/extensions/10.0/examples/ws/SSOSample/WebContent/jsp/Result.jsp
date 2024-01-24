<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.ibm.itim.ws.model.WSPerson" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<font size='6' color='blue'>Result of Webcall</font><hr>
       <%
       WSPerson person= (WSPerson)request.getSession().getAttribute("result");
       %>
       Name        :<%=person.getName() %><br>
       ProfileName :<%=person.getProfileName() %><br>
       Person DN   :<%=person.getItimDN() %>
       
</body>
</html>