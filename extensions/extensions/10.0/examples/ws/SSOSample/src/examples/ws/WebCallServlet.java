/************************************************************************
* IBM Confidential
* OCO Source Materials
* *** IBM Security Identity Manager ***
*
* (C) Copyright IBM Corp. 2015  All Rights Reserved.
*
* The source code for this program is not published or otherwise  
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office.
*************************************************************************/
package examples.ws;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSInvalidSessionException;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSPersonServiceService;



/**
 * Servlet implementation class WebCallServlet
 * This servlet invokes the webservice call
 */
public class WebCallServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String PROP_FILE="/WEB-INF/lib/ws_example_config.properties";
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
	 public void init(ServletConfig config) throws ServletException{
                super.init(config);
     }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ltpaToken=(String)request.getSession().getAttribute("Ltpa");
		String webSealCheck=request.getParameter(ClientConstants.REQ_PARAM_WSCALLTYPE);
		boolean isWebSEALPresent = false;
		if(webSealCheck != null && ClientConstants.WEBSEAL.equalsIgnoreCase(webSealCheck)){
			isWebSEALPresent=true;
		}
		
		//Check if we are using webSEAL for protecting the ISIM web services.
		String enpointURL = this.constructWSEndpointURL(request,isWebSEALPresent);
		invokeWSAPI(request, ltpaToken, isWebSEALPresent, enpointURL); 
		
		RequestDispatcher rd = getServletContext().getRequestDispatcher("/jsp/Result.jsp");
		rd.forward(request, response); 
        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void invokeWSAPI(HttpServletRequest request, String ltpaToken,
			boolean isWebSEALPresent, String enpointURL) {
		WSSession wsSession=new WSSession();
		wsSession.setSessionID(0L);
		WSPersonServiceService service=new WSPersonServiceService();
		
		//Set the client side handler to intercept the SOAP message.
		service.setHandlerResolver(new HandlerResolver() {
			@Override
			public List<Handler> getHandlerChain(PortInfo portInfo) {
				List<Handler> handlerList = new ArrayList<Handler>();
		        Handler handler = new examples.ws.ClientHandler();
		        handlerList.add(handler);
		        return handlerList;
			}
     
		  });
		WSPersonService proxy=service.getWSPersonService();
		
		//Pass the ltpa token in the request context map.This can be extracted in the client handler.
		Map<String, Object> requestContext = ((BindingProvider)proxy).getRequestContext();
		requestContext.put(SecurityHeader.LTPA, ltpaToken);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, enpointURL);
		requestContext.put(ClientConstants.REQ_PARAM_WSCALLTYPE, isWebSEALPresent);
		
		try {
			
			  WSPerson wsPerson=proxy.getPrincipalPerson(wsSession);
			  request.getSession().setAttribute("result",wsPerson);
			
		} catch (WSApplicationException e) {
			e.printStackTrace();
		} catch (WSInvalidSessionException e) {
			e.printStackTrace();
		}
	}
	
	private String constructWSEndpointURL(HttpServletRequest request, boolean isWebSEALPresent){
		String hostName = null;
		String port = null;
		String junction = null;
		String endpointURL = null;
		
		if(isWebSEALPresent){
			hostName = request.getParameter(ClientConstants.REQ_PARAM_WEBSEAL_HOST);
			port = request.getParameter(ClientConstants.REQ_PARAM_WEBSEAL_PORT);
			junction=request.getParameter(ClientConstants.REQ_PARAM_WEBSEAL_JUNCTION);
			
			if(port != null && !"80".equals(port)){
				endpointURL = "http://" + hostName + ":" + port + "/"+ junction +"/itim/services/WSPersonServiceService";
			}else{
				endpointURL = "http://" + hostName + "/" + junction + "/itim/services/WSPersonServiceService";
			}
		}else{
			hostName = request.getParameter(ClientConstants.REQ_PARAM_ISIM_WS_HOST);
			port = request.getParameter(ClientConstants.REQ_PARAM_ISIM_WS_PORT);
			
			endpointURL = "http://"+ hostName + ":" + port + "/itim/services/WSPersonServiceService";
		}
		
		return endpointURL;
	}

}
