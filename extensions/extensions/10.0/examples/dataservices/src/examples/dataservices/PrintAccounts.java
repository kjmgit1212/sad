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
 *                         Access360, Irvine, CA                  		*
 *                          All rights reserved.                         *
 *                                                                       *
 ************************************************************************/

package examples.dataservices;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountSearch;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.dataservices.model.domain.ServiceEntity;

public class PrintAccounts {

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("Identity's dn must be entered");
			return;
		}

		printAccounts(args[0]);
	}

	public static void printAccounts(String rawIdentityDN) {

		try {
			// Create compatible object from raw dn.
			DistinguishedName identityDN = new DistinguishedName(rawIdentityDN);

			// lookup accounts
			Collection accounts = (new AccountSearch()).searchByOwner(identityDN);

			Iterator iter = accounts.iterator();
			// Loop through accounts and print user id and service name
			while (iter.hasNext()) {
				AccountEntity accountEnt = (AccountEntity) iter.next();
				Account account = (Account) accountEnt.getDirectoryObject();
				ServiceEntity serviceEnt = accountEnt.getService();
				Service service = (Service) serviceEnt.getDirectoryObject();

				System.out.println("*****Account*****");
				System.out.println("User Id: " + account.getUserId());
				System.out.println("Service: " + service.getName());
				System.out.println();
			}
		} catch (ModelCommunicationException mce) {
			System.err.println("Communication Exception: " + mce.toString());
		} catch (ObjectNotFoundException onfe) {
			System.err.println("Object Not Found Exception: " + onfe.toString());
		}
	}
}
