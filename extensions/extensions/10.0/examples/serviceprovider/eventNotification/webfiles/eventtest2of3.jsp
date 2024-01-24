<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
     "http://www.w3.org/TR/html4/strict.dtd">

<%-- ********************************************************************* 
                                                                       
     Licensed Materials - Property of IBM                                  
                                                                       
     Source File Name = eventtest2of3.jsp                                  
                                                                       
     (C) COPYRIGHT IBM Corp. 2003, 2007                                          
                                                                       
     US Government Users Restricted Rights - Use, duplication or           
     disclosure restricted by GSA ADP Schedule Contract with IBM Corp.     
                                                                       
     ********************************************************************* --%>

<%-- Displays the second screen for testing of the event notification API. 
     This will enable the user to enter details about the account event to 
     be generated.                                                         --%>

<%@ page import="com.ibm.itim.dataservices.model.domain.Service"%>

<html>
   <head>
      <title>Event Notification Test</title>
      <link href="event.css" rel="stylesheet" type="text/css">
   </head>
   <body>
      <h1>Event Notification Test</h1>
      2 of 3
      <h2>Account Operation Screen</h2>
      <%
      Service service = (Service)session.getAttribute("service");
      String serviceName = null;
      if (service != null) {
          serviceName = service.getName();
      }
      %>
      <p>Service: <%=serviceName%>
      <p>Please enter service data:
      <form method="POST" action="eventtestrouter.jsp">
         <table padding="5">
            <tr>
               <td><label for="operation">Operation</label></td>
               <td>
                  <select size='3' name="operation">
                     <option selected value="add">add</option>
                     <option value="delete">delete</option>
                     <option value="modify">modify</option>
                  </select>
               </td>
            </tr>
            <tr>
               <td><label for="userid">User ID</label></td>
               <td><input value="gwhitlam" name="userid" 
                          size="30"></td>
            </tr>
            <tr>
               <td><label for="objectclass">Object Class</label></td>
               <td><input value="erJNDISampleUserAccount" 
                          name="objectclass" size="30"></td>
            </tr>
            <tr>
               <td colspan="2" align="center">
                  <input value="submit" name="submit" type="submit">
               </td>
            </tr>
         <table> 
      </form>
   </body>
</html>


