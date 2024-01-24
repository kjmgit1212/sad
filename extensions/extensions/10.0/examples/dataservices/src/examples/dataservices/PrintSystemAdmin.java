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
import com.ibm.itim.dataservices.model.DirectoryObjectEntity;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ModelIntegrityException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountSearch;
import com.ibm.itim.dataservices.model.domain.DirectorySystemEntity;
import com.ibm.itim.dataservices.model.domain.DirectorySystemSearch;
import com.ibm.itim.dataservices.model.domain.Person;
import com.ibm.itim.dataservices.model.domain.PersonEntity;
import com.ibm.itim.dataservices.model.domain.PersonSearch;
import com.ibm.itim.dataservices.model.domain.RoleEntity;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.dataservices.model.domain.ServiceEntity;

public class PrintSystemAdmin {
	public static void main(String[] args) {
		try {
			// Get default system (tenant) context
			DirectorySystemEntity context = new DirectorySystemSearch()
					.lookupDefault();
			CompoundDN logicalContext = new CompoundDN(context
					.getDistinguishedName());

			// Find ITIM Manager using ldap filter
			SearchResults identities = new PersonSearch().searchByFilter(
					logicalContext, "(cn=System Administrator)",
					new SearchParameters());

			// If not found exit
			if (identities.isEmpty()) {
				System.err.println("Cannot find ITIM Manager");
				return;
			}

			// Get first element which is sys admin
			PersonEntity managerEnt = (PersonEntity) identities.iterator().next();
			Person manager = (Person) managerEnt.getDirectoryObject();

			// print summary information
			System.out.println("********System Administrator Summary********");
			System.out.println();
			System.out.println("Name: " + manager.getName());
			System.out.println("Email: " + manager.getMail());
			System.out.println("Status: " + manager.getStatus());

			// Get the System Administrator's container
			DirectoryObjectEntity dirObjEnt = managerEnt.getParent();
			if (dirObjEnt != null) {
				DirectoryObject dirObj = dirObjEnt.getDirectoryObject();
				System.out.println("Container: " + dirObj.getName());
			}

			System.out.println();

			// print roles
			Collection roles = managerEnt.getRoles();

			if (!roles.isEmpty()) {
				System.out.println("**********Roles**********");
				System.out.println();

				Iterator iter = roles.iterator();
				while (iter.hasNext()) {
					RoleEntity roleEnt = (RoleEntity) iter.next();
					System.out.println(roleEnt.getDirectoryObject().getName());
				}

				System.out.println();
			}

			// Print admin entry's ldap attributes.
			printDirectoryObject(manager);

			System.out.println();

			// Retrieve all accounts for sys admin
			DistinguishedName dn = managerEnt.getDistinguishedName();
			Collection accounts = (new AccountSearch()).searchByOwner(dn);

			// Print accounts
			if (!accounts.isEmpty()) {
				System.out.println("**********Accounts**********");

				Iterator iter = accounts.iterator();

				int i = 1;

				while (iter.hasNext()) {
					System.out.println("\n***Account " + i++ + "***\n");

					// Print summary
					AccountEntity accountEnt = (AccountEntity) iter.next();
					Account account = (Account) accountEnt.getDirectoryObject();

					ServiceEntity serviceEnt = accountEnt.getService();
					Service service = (Service) serviceEnt.getDirectoryObject();
					System.out.println("User Id: " + account.getUserId());
					System.out.println("Service: " + service.getName());

					int status = account.getStatus();
					switch (status) {
					case Account.ACTIVE_STATUS:
						System.out.println("Status: " + status + " (Active)");
						break;
					case Account.INACTIVE_STATUS:
						System.out.println("Status: " + status + " (Inactive)");
						break;
					default:
						System.out.println("Status: " + status + " (Unknown)");
					}

					System.out.println();

					// print account entry's ldap attributes
					printDirectoryObject(account);
				}
			}
		} catch (ModelCommunicationException e) {
			System.err.println("Cannot communicate with server: " + e.toString());
		} catch (PartialResultsException e) {
			System.err.println("Error retrieving results: " + e.toString());
		} catch (ModelIntegrityException e) {
			System.err.println("Internal server error: " + e.toString());
		} catch (ObjectNotFoundException e) {
			System.err.println("Internal server error: " + e.toString());
		}
	}

	// prints all attributes of an extensible entry
	private static void printDirectoryObject(DirectoryObject dirObj) {
		// get collection of attributes
		AttributeValues attributes = dirObj.getAttributes();

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
