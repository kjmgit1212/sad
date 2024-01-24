<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
     "http://www.w3.org/TR/html4/strict.dtd">

<%-- ********************************************************************* 
                                                                       
     Licensed Materials - Property of IBM                                  
                                                                       
     Source File Name = eventtest2of3.jsp                                  
                                                                       
     (C) COPYRIGHT IBM Corp. 2003, 2007                                          
                                                                       
     US Government Users Restricted Rights - Use, duplication or           
     disclosure restricted by GSA ADP Schedule Contract with IBM Corp.     
                                                                       
     ********************************************************************* --%>

<%-- Displays the third screen for testing of the event notification API. 
     This will enable the user to enter details about the account event that 
     was submitted in the previous step.                                   
--%>

<%@ page import="com.ibm.itim.remoteservices.provider.RequestStatus"%>

<html>
   <head>
      <title>Event Notification Test</title>
      <link href="event.css" rel="stylesheet" type="text/css">
   </head>
   <body>
      <h1>Event Notification Test</h1>
      3 of 3
      <h2>Confirmation Screen</h2>
      <%
      RequestStatus status = (RequestStatus)request.getAttribute("status");
      if (status != null) {
          %>
          <p>Completed operation.  Successful: <%=status.succeeded() %>.
	  <%
          if (!status.succeeded()) {
          %>
              <p>Message key: <%=status.getReasonMessage()%>
              <%
              if (!status.getReasonMessageArgs().isEmpty()) {
              %>
                  Message arguments: <%=status.getReasonMessageArgs()%>
              <%
              }
          }
      } else {
          %>
          <p>status = null
          <%
      }
      %>
      <form method="POST" action="eventtestrouter.jsp">
         <input value="Go back to the beginning" name="submit" type="submit">
         <input value="startAgain" name="operation" type="hidden">
      </form>
   </body>
</html>


