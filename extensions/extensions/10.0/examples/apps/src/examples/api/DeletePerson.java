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

package examples.api;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Person;

public class DeletePerson {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PERSON_FILTER = "personfilter";

	private static final String[][] utilParams = new String[][] { {
			PERSON_FILTER, "No personfilter specified" } };

	public static void main(String[] args) {
		run(args, true);
	}

	/**
	 * Run the example.
	 * 
	 * @param args
	 *            Arguments passed in, usually from the command line. See usage
	 *            from more information.
	 * @param verbose
	 *            Should the program print out lots of information.
	 * @return true if run() completes successfully, false otherwise.
	 */
	public static boolean run(String[] args, boolean verbose) {

		Utils utils = null;
		Hashtable<String, Object> arguments = null;
		PlatformContext platform = null;

		try {
			utils = new Utils(utilParams, verbose);
			arguments = utils.parseArgs(args);
		} catch (IllegalArgumentException ex) {
			if (verbose) {
				System.err.println(getUsage(ex.getMessage()));
			}
			return false;
		}

		try {
			String tenantId = utils.getProperty(Utils.TENANT_ID);
			String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);

			platform = utils.getPlatformContext();
			Subject subject = utils.getSubject();

			utils.print("Searching for Person \n");
			// Use the Search API to locate the Person to delete
			String personFilter = (String) arguments.get(PERSON_FILTER);
			SearchMO searchMO = new SearchMO(platform, subject);
			searchMO.setCategory(ObjectProfileCategory.PERSON);
			String TENANT_DN = "ou=" + tenantId + "," + ldapServerRoot;
			searchMO
					.setContext(new CompoundDN(new DistinguishedName(TENANT_DN)));
			searchMO.setProfileName("Person");
			searchMO.setFilter(personFilter);
			SearchResultsMO searchResultsMO = null;
			Collection people = null;
			try {
				searchResultsMO = searchMO.execute();
				people = searchResultsMO.getResults();
				if (people.size() == 0) {
					utils.print("Unable to find person");
					return false;
				}
			} finally {
				// close SearchResultsMO
				if(searchResultsMO != null) {
					try {
						searchResultsMO.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			Person person = (Person) people.iterator().next();

			// create a PersonMO with the owner's DistinguishedName
			PersonMO personMO = new PersonMO(platform, subject, person
					.getDistinguishedName());

			utils.print("Submitting request to delete Person\n");

			Request request = personMO.remove(null);

			utils.print("Request submitted. Process Id: " + request.getID());

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (SchemaViolationException e) {
			e.printStackTrace();
		} catch (AuthorizationException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
			e.printStackTrace();
		} finally {
			if(platform != null) {
				platform.close();
				platform = null;
			}
		}
		
		return true;
	}

	public static String getUsage(String msg) {
		StringBuffer buf = new StringBuffer();
		buf.append("\ndeletePerson: " + msg + "\n");
		buf.append("usage: deletePerson -[argument] ? \"[value]\"\n");
		buf.append("\n");
		buf.append("-personfilter\t");
		buf.append("Ldap Filter to search for the Person to delete\n\n");
		buf.append("Example: deletePerson -personfilter?\"(cn=Jenny Brown)\" ");

		return buf.toString();
	}
}
