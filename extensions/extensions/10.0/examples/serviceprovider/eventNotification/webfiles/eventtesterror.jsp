<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
     "http://www.w3.org/TR/html4/strict.dtd">

<%-- ********************************************************************* 
                                                                       
     Licensed Materials - Property of IBM                                  
                                                                       
     Source File Name = eventtesterror.jsp                                  
                                                                       
     (C) COPYRIGHT IBM Corp. 2003, 2007                                          
                                                                       
     US Government Users Restricted Rights - Use, duplication or           
     disclosure restricted by GSA ADP Schedule Contract with IBM Corp.     
                                                                       
     ********************************************************************* --%>

<%-- 
     Displays an error screen for testing of the event notification API.
--%>

<html>
   <head>
      <title>Event Notification Test Error</title>
      <link href="event.css" rel="stylesheet" type="text/css">
   </head>
   <body>
      <h1>Event Notification Test Error</h1>
      <%
      Exception e = (Exception)request.getAttribute("error");
      if (e != null) {
      %>
          <p>Exception Type: <%=e.getClass()%>
          <p>Exception Message: <%=e.getMessage()%>
          <p>Stack Trace:
          <p>
          <%  
              String stackTrace = null;
              try {
                  java.io.StringWriter sWriter = new java.io.StringWriter();
                  java.io.PrintWriter writer = new java.io.PrintWriter(sWriter);
                  e.printStackTrace(writer);
                  writer.flush();
                  stackTrace = sWriter.toString();
                  writer.close();
                  sWriter.close();
              } catch(Exception ex) {
                  System.err.println("Could not print stack trace to page");
                  e.printStackTrace();
              }
          %>
          <%=stackTrace%>
      <%}%>
      <p>Go back to the <a href="eventtest1of3.jsp">start page</a>.
   </body>
</html>


