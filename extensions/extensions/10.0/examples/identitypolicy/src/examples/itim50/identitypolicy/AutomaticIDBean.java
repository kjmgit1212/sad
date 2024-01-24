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
package examples.itim50.identitypolicy;

import java.util.HashSet;
import java.util.Set;

import com.ibm.itim.dataservices.dit.DITLayoutManager;
import com.ibm.itim.dataservices.dit.NamingAuthority;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DirectoryObject;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.SearchResultsIterator;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountSearch;
import com.ibm.itim.dataservices.model.domain.ServiceEntity;
import com.ibm.itim.script.ExtensionBean;
import com.ibm.itim.script.ScriptContextDAO;
import com.ibm.itim.script.extensions.ServiceScriptInterface;

/**
 * AutomaticIDBean provides the implementation of the
 * <code>getNextCount()</code> method. There are several private methods in
 * this class that <code>getNextCount()</code> uses to do its work.
 */
public class AutomaticIDBean implements ExtensionBean {
	/**
	 * Controls if the debug statements are printed out to the console. Change
	 * this to true to see the output.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Constant used in building LDAP filters.
	 */
	private static final String ACCOUNT_KEY = "eruid";

	/**
	 * Constant used in building LDAP filters.
	 */
	private static final String SERVICE_KEY = "erservice";

	/**
	 * The largest possible number for an account suffix.
	 */
	private static final int MAX_SUFFIX = 100;

	/**
	 * We should return this value if we want to use the baseId.
	 */
	private static final int USE_BASE_ID = -1;

	/**
	 * Return this value if an error occurs.
	 */
	private static final int ERR = -2;

	private ScriptContextDAO dao;

	private SearchParameters searchParams;

	private AccountSearch accountSearch;

	/**
	 * Create a new AutomaticIDBean. To create an AutomaticIDBean, you need to
	 * have a <code>ScriptContextDAO</code> object that contains a
	 * <code>ServiceEntity</code> object that can be accessed with the name
	 * <code>ServiceScriptInterface.SCRIPT_CONTEXT_ITEM_SERVICE</code>. If
	 * the <code>ServiceEntity</code> does not exist then
	 * <code>getNextCount()</code> will not work.
	 * 
	 * @param dao
	 *            The ScriptContextDAO object to use.
	 * 
	 * @see ScriptContextDAO
	 * @see ServiceScriptInterface.SCRIPT_CONTEXT_ITEM_SERVICE
	 */
	public AutomaticIDBean(ScriptContextDAO dao) {
		this.dao = dao;

		accountSearch = new AccountSearch();

		searchParams = new SearchParameters();
		searchParams.setScope(SearchParameters.SUBTREE_SCOPE);
	}

	/**
	 * Get the next count for a suffix to add to baseId to make it a unique ID.
	 * For example, if accounts with the following ids (jdoe, jdoe1, and jdoe2)
	 * exists on the service then getNextCount() for a baseId of "jdoe" would be
	 * 3. In the case where there is a gap in the number (jdoe, jdoe1, and
	 * jdoe3) then getNextCount() will return 2, and the next call will return
	 * 4.
	 * 
	 * @param baseId
	 *            The baseId to check. This can be any String.
	 * @param serviceId
	 *            The service's global id. This is the erglobalid of the service
	 *            object.
	 * @return The next number to use to create a unique ID, or -1 if the baseId
	 *         should be used, or -2 if an error occurs.
	 */
	public int getNextCount(String baseId, String serviceId) {

		int result = -1;

		if (serviceId.length() == 0) {
			serviceId = "*";
		} else {
			serviceId = "*" + serviceId + "*";
		}

		try {
			if (!isBaseIdUsed(baseId, serviceId)) {
				return USE_BASE_ID;
			}

			result = getLowestUnusedInt(baseId, serviceId);
		} catch (PartialResultsException e) {
			result = ERR;
		} catch (ModelCommunicationException e) {
			result = ERR;
		} catch (ObjectNotFoundException e) {
			result = ERR;
		}

		trace("getNextCount - result = " + result);
		return result;
	}

	/**
	 * Check to see if the baseId is being used for the given service.
	 * 
	 * @param baseId
	 *            The baseId passed into getNextCount().
	 * @param serviceId
	 *            The serviceId passed into getNextCount().
	 * @return true if baseId is used for the service, false otherwise.
	 * 
	 * @throws ModelCommunicationException
	 *             Thrown if there is an error.
	 * @throws ObjectNotFoundException
	 *             Thrown if there is an error.
	 * @throws PartialResultsException
	 *             Thrown if there is an error.
	 */
	private boolean isBaseIdUsed(String baseId, String serviceId)
			throws ModelCommunicationException, ObjectNotFoundException,
			PartialResultsException {
		String filter = "(&(" + ACCOUNT_KEY + "=" + baseId + ")(" + SERVICE_KEY
				+ "=" + serviceId + "))";
		trace("isBaseIdUsed - filter: " + filter);

		SearchResults results = accountSearch.searchByFilter(
				getSearchContext(), filter, searchParams);

		SearchResultsIterator itr = results.iterator();

		// If there were any results to our search then id is used.
		return itr.hasNext();
	}

	/**
	 * Search through the service for each id that starts with baseId to find if
	 * we have any in the form baseId* where * is a number. For each * that is a
	 * number put that number in a Set so that we can skip it later.
	 * 
	 * @param baseId
	 *            The baseId passed into getNextCount().
	 * @param serviceId
	 *            The serviceId passed into getNextCount().
	 * @return A Set of Integers that holds all the numbers that are already
	 *         used.
	 * @throws ObjectNotFoundException
	 *             Thrown if an error occurs.
	 * @throws ModelCommunicationException
	 *             Thrown if an error occurs.
	 * @throws PartialResultsException
	 *             Thrown if an error occurs.
	 */
	private Set<Integer> getUsedIds(String baseId, String serviceId)
			throws ObjectNotFoundException, ModelCommunicationException,
			PartialResultsException {
		String filter = "(&(" + ACCOUNT_KEY + "=" + baseId + "*)("
				+ SERVICE_KEY + "=" + serviceId + "))";
		trace("createCacheEntry - filter: " + filter);

		SearchResults results = accountSearch.searchByFilter(
				getSearchContext(), filter, searchParams);

		Set<Integer> entry = new HashSet<Integer>();

		for (SearchResultsIterator itr = results.iterator(); itr.hasNext();) {
			DirectoryObject account = ((AccountEntity) itr.next())
					.getDirectoryObject();
			String uid = account.getAttribute(ACCOUNT_KEY).getString();

			/*
			 * The following if condition can be satisfied for the 2 cases : (1)
			 * baseId="jsmith" and eruid="jsmith1" or (2) baseId="jsmith" and
			 * eruid="jsmithson"
			 */
			if (uid.length() > baseId.length()) {
				String suffix = uid.substring(baseId.length(), uid.length());
				try {
					/*
					 * Parse to shake out the non-numbers. If a
					 * NumberFormatException is not thrown then we have case 1,
					 * otherwise we have case 2.
					 */
					entry.add(Integer.parseInt(suffix));
				} catch (NumberFormatException e) {
					; // Do nothing.
				}
			}
		}
		return entry;
	}

	/**
	 * Get the lowest unused number for the given baseId and serviceId. This
	 * method not only finds the lowest number, but inserts that number into the
	 * cache entry so future calls do not return the same number.
	 * 
	 * @param baseId
	 *            The baseId passed into getNextCount().
	 * @param serviceId
	 *            The serviceId passed into getNextCount().
	 * @return The lowest unused number. -2 if an error occurs.
	 * @throws ModelCommunicationException
	 * @throws ObjectNotFoundException
	 * @throws PartialResultsException
	 */
	private int getLowestUnusedInt(String baseId, String serviceId)
			throws PartialResultsException, ObjectNotFoundException,
			ModelCommunicationException {
		Set<Integer> existingIds = getUsedIds(baseId, serviceId);

		for (int i = 1; i < MAX_SUFFIX; i++) {
			if (!existingIds.contains(i)) {
				return i;
			}
		}

		return ERR;
	}

	/**
	 * Get the search context by looking at the service.
	 * 
	 * @return A compoundDN to use as the searchContext in an ldap filter.
	 */
	private CompoundDN getSearchContext() {
		ServiceEntity serviceEnt = (ServiceEntity) dao
				.lookupItem(ServiceScriptInterface.SCRIPT_CONTEXT_ITEM_SERVICE);
		DirectoryObject service = serviceEnt.getDirectoryObject();

		// Get the search context
		NamingAuthority na = DITLayoutManager.getInstance().getLayout()
				.getNamingAuthority();
		DistinguishedName tenantDN = na.getTenantDN(service
				.getDistinguishedName());
		DistinguishedName orgDN = na.getOrganizationDN(service
				.getDistinguishedName());

		CompoundDN searchContext = new CompoundDN();
		searchContext.append(tenantDN);
		searchContext.append(orgDN);

		return searchContext;
	}

	/**
	 * If DEBUG is set to true, print out the message passed in.
	 * 
	 * @param msg
	 *            The message to print.
	 */
	private void trace(String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}
}
