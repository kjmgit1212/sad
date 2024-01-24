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
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * 
 * This is the client side handler.It itercepts the outgoing SOAP message and  injects
 * the LTPA token into the SOAP header.
 *
 */

public class ClientHandler implements SOAPHandler<SOAPMessageContext>
{
	
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		
		boolean isWebSEALPresent = (Boolean)context.get(ClientConstants.REQ_PARAM_WSCALLTYPE);
		
		boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if (outbound) {
			String ltpaToken=(String)context.get(SecurityHeader.LTPA);
			if(!isWebSEALPresent){
				SOAPElement headerElement=SecurityHeader.createLTPAHeader(ltpaToken);
				SOAPEnvelope envelope=null;
				try {	
				  envelope = context.getMessage().getSOAPPart().getEnvelope();
				  SOAPHeader header = envelope.addHeader();
		          header.addChildElement(headerElement);
		          context.getMessage().writeTo(System.out);
				} catch (SOAPException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				//Requesting the webservice operation via web seal
			}
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

}
