<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
     "http://www.w3.org/TR/html4/strict.dtd">

<%-- ********************************************************************* 
                                                                       
    Licensed Materials - Property of IBM                                  
                                                                      
    Source File Name = eventtestrouter.jsp                                  
                                                                       
    (C) COPYRIGHT IBM Corp. 2003, 2007                                          
                                                                       
    US Government Users Restricted Rights - Use, duplication or           
    disclosure restricted by GSA ADP Schedule Contract with IBM Corp.     
                                                                       
    ********************************************************************* --%>

<%--
    Example jsp to illustrate use of event notification API.  The example
    will look up a service and use the event notification API to notify TIM
    of account changes.  This page listens for HTTP events and dispatches them
    to delegates classes to do the work of invoking the notification API
    methods.
 --%>
<%@ page import="examples.serviceprovider.eventNotification.EventNotifier"%>
<%
    EventNotifier notifier = new EventNotifier();
    String nextPage = notifier.service(request, response);
%>
<jsp:forward page="<%=nextPage%>"/>


