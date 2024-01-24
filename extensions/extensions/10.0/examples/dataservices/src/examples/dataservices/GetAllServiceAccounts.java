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

/************************************************************************
 *                                                                       *
 *                         Copyright (c) 2002 by                         *
 *                         Access360, Irvine, CA                         *
 *                          All rights reserved.                         *
 *                                                                       *
 ************************************************************************/

package examples.dataservices;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValueIterator;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DirectoryObject;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.SearchResultsIterator;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountTable;
import com.ibm.itim.dataservices.model.domain.DirectorySystemEntity;
import com.ibm.itim.dataservices.model.domain.DirectorySystemSearch;
import com.ibm.itim.dataservices.model.domain.ServiceEntity;
import com.ibm.itim.dataservices.model.domain.ServiceSearch;

public class GetAllServiceAccounts {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Service name must be entered");
			return;
		}

		try {
			// Get default system (tenant) context
			DirectorySystemEntity context = new DirectorySystemSearch()
					.lookupDefault();
			CompoundDN logicalContext = new CompoundDN(context
					.getDistinguishedName());

			// Find service with passed name
			SearchResults services = new ServiceSearch().searchByFilter(
					logicalContext, "", "(erservicename=" + args[0] + ")",
					true, new SearchParameters());

			if (services.isEmpty()) {
				System.err.println("No service found with that name");
				return;
			} else {

				AccountTable table = new AccountTable((ServiceEntity) services
						.iterator().next());

				SearchResults accounts = table.getAll();

				SearchResultsIterator iter = accounts.iterator();

				while (iter.hasNext()) {
					// print account entry's ldap attributes
					printDirectoryObject(((AccountEntity) iter.next())
							.getDirectoryObject());
				}
			}
		} catch (ModelCommunicationException mce) {
			System.err.println("Cannot communicate with server: "
					+ mce.toString());
		} catch (PartialResultsException pre) {
			System.err.println("Some accounts were not returned: "
					+ pre.toString());
		} catch (ObjectNotFoundException onfe) {
			System.err.println("Cannot find entity: " + onfe.toString());
		}
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
