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

package examples.dataservices.extensions;

import java.util.Collection;
import java.util.HashSet;

import com.ibm.itim.dataservices.extensions.IPersonExtension;
import com.ibm.itim.dataservices.model.domain.Person;

/**
 * This example class demonstrates how to write an extension plug-in for persons.
 * When the compiled class is placed in the classpath, and enroleExtensionAttributes.properties
 * is configured to point to this class, it will be called by the server to retrieve the
 * value for each person's image URI.
 */
public class CustomPersonPlugin implements IPersonExtension 
{
	private final String IMAGE_ATTRNAME = "erImageURI";
	private final String IMAGE_DEPENDENT_ATTRNAME = "mail";
	private final String IMAGE_BASEURI = "http://images.myserver.com/";

	
	/**
	 * Returns dependent attributes.
	 */
	public Collection<String> getDependentAttributes(String categoryName, String profileName, String attrName) 
	{
		HashSet<String> dependents = new HashSet<String>();
		dependents.add(IMAGE_DEPENDENT_ATTRNAME);
		return dependents;
	}

	/**
	 * Returns the person's image URI.
	 * Sample URI - http://images.myserver.com/lookup?email=jdoe@myserver.com
	 */
	public Object getAttributeValue(Person person, String attrName) 
	{
		if (attrName.equalsIgnoreCase(IMAGE_ATTRNAME) && person.getMail() != null)
			return IMAGE_BASEURI + "lookup" + "?email=" + person.getMail();
		return null;
	}
}
