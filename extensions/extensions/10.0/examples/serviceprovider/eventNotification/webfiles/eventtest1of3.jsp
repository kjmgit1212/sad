<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
     "http://www.w3.org/TR/html4/strict.dtd">

<%-- ********************************************************************* 
                                                                       
     Licensed Materials - Property of IBM                                  
                                                                       
     Source File Name = eventtest2of3.jsp                                  
                                                                       
     (C) COPYRIGHT IBM Corp. 2003, 2007                                          
                                                                       
     US Government Users Restricted Rights - Use, duplication or           
     disclosure restricted by GSA ADP Schedule Contract with IBM Corp.     
                                                                       
     ********************************************************************* --%>

<%-- Displays the initial screen for testing of the event notification API.
     This will enable the user to enter details about the service to be 
     tested.                                                               --%>

<html>
   <head>
      <title>Event Notification Test</title>
      <link href="event.css" rel="stylesheet" type="text/css">
   </head>
   <body>
      <h1>Event Notification Test</h1>
      1 of 3
      <h2>Service Lookup Screen</h2>
      <p>Please enter service data:
      <form method="POST" action="eventtestrouter.jsp">
         <table padding="5">
            <tr>
               <td><label for="tenant">Tenant</label></td>
               <td><input value="ups" name="tenant"></td>
            </tr>
            <tr>
               <td><label for="serviceFilter">Service Filter</label></td>
               <td><input value="(erServiceName=JNDI)" name="serviceFilter" 
                          size="30"></td>
            </tr>
            <tr>
               <td><label for="principal">Principal</label></td>
               <td><input value="agent" name="principal"></td>
            </tr>
            <tr>
               <td><label for="password">Password</label></td>
               <td><input value="agent" name="password" type="password"></td>
            </tr>
            <tr>
               <td colspan="2" align="center">
                  <input value="get_service" name="operation" type="hidden">
                  <input value="submit" name="submit" type="submit">
               </td>
            </tr>
         <table> 
      </form>
   </body>
</html>


