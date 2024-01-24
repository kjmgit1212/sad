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

package examples.dataservices;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValueIterator;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DirectoryObject;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.domain.DirectorySystemEntity;
import com.ibm.itim.dataservices.model.domain.DirectorySystemSearch;
import com.ibm.itim.dataservices.model.system.SystemUserEntity;
import com.ibm.itim.dataservices.model.system.SystemUserSearch;

public class FindEnroleUser {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("User Id must be entered");
			return;
		}

		try {
			// Get default system (tenant) context
			DirectorySystemEntity context = new DirectorySystemSearch()
					.lookupDefault();

			SystemUserEntity user = findWithSystemUserSearch(context, args[0]);

			// print system user entry's ldap attributes
			printDirectoryObject(user.getDirectoryObject());
		} catch (ModelCommunicationException mce) {
			System.err.println("Cannot communicate with server: "
					+ mce.toString());
		} catch (ObjectNotFoundException onfe) {
			System.err.println("Unable to find user");
		}
	}

	// Uses SystemUserSearch interface to retrieve a SystemUser. This is
	// a much simpler approach to use if only dealing with enRole.
	private static SystemUserEntity findWithSystemUserSearch(
			DirectorySystemEntity context, String userId)
			throws ModelCommunicationException, ObjectNotFoundException {
		SystemUserSearch search = new SystemUserSearch();
		System.out.println("user id: " + userId);

		return search.searchByUserID(context.getDistinguishedName(), userId);
	}

	// prints all attributes of an extensible entry
	private static void printDirectoryObject(DirectoryObject o) {
		// get collection of attributes
		AttributeValues attributes = o.getAttributes();

		if (!attributes.isEmpty()) {
			System.out.println("**********LDAP Attributes**********");

			AttributeValueIterator attrIter = attributes.iterator();

			while (attrIter.hasNext()) {
				System.out.println();

				AttributeValue attr = (AttributeValue) attrIter.next();

				// print name
				System.out.println("Name: " + attr.getName());

				// get values (could be multi-valued)
				Collection values = attr.getValues();

				if (!values.isEmpty()) {
					Iterator valIter = values.iterator();

					// print each value
					while (valIter.hasNext())
						System.out.println(valIter.next().toString());
				}
			}
		}
	}
}
