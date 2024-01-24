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



import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;

import com.ibm.wsspi.wssecurity.core.token.config.WSSConstants;

public class SecurityHeader {
	
	private static final String WS_ISIM_60_ACTOR = "http://services.ws.itim.ibm.com/60/actor";
    private static String LTPAns = "wsst";
    private static String LTPAid = ":LTPA";
    private static String LTPAURL = 
                       "http://www.ibm.com/websphere/appserver/tokentype/5.0.2";
    private static String SOAPEncURL = "htp://schemas.xmlsoap.org/soap/encoding/";
    private static String SOAPEnvURL = "http://schemas.xmlsoap.org/soap/envelope/";
    public  static String LTPA="ltpaToken";
    private static QName WSSE_SECURITY_ELEMENT_QNAME = new QName(WSSConstants.Namespace.WSSE, "Security","wsse");	
	private static QName WSSE_BINARY_TOKEN_ELEMENT_QNAME = new QName(WSSConstants.Namespace.WSSE, "BinarySecurityToken","wsse");
	private static QName WSSE11_SECURITY_ELEMENT_QNAME = new QName(WSSConstants.Namespace.WSSE11, "Security");
	private static QName WSSE11_BINARY_TOKEN_ELEMENT_QNAME = new QName(WSSConstants.Namespace.WSSE11, "BinarySecurityToken");
    // Create LTPA token header
    public static SOAPElement createLTPAHeader(String token){
		
        if(token == null)
            return null;
			
        // Now try to build the header
        SOAPElement header = null;
        try{
			
        	 SOAPFactory sFactory = SOAPFactory.newInstance();
 			 header=   sFactory.createElement(WSSE_SECURITY_ELEMENT_QNAME);
 			 header.addNamespaceDeclaration("soapenc",SOAPEncURL);
 			 header.addNamespaceDeclaration("xsd",XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			 header.addNamespaceDeclaration("xsi",XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
 			 Name mustName = sFactory.createName("mustUnderstand","soapenv",SOAPEnvURL);
 			 header.addAttribute(mustName,"0");
 			 header.addAttribute(new QName(SOAPEnvURL, "actor"), WS_ISIM_60_ACTOR);
 		     SOAPElement binaryToken= sFactory.createElement(WSSE_BINARY_TOKEN_ELEMENT_QNAME);
 		     binaryToken.addNamespaceDeclaration(LTPAns,LTPAURL);
 		     Name attrName = sFactory.createName("ValueType");
 		     binaryToken.addAttribute(attrName,LTPAns+LTPAid);
 		     binaryToken.addTextNode(token);	
 		     header.addChildElement(binaryToken);
       }
        catch(Exception e){
            e.printStackTrace();
        }
        return header;
    }
	

} 

