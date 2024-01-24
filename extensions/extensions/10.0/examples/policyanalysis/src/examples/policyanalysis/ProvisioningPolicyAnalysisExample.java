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

package examples.policyanalysis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DirectoryObject;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ModelException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.ObjectProfile;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.ProfileLocator;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.SearchResultsIterator;
import com.ibm.itim.dataservices.model.domain.DirectorySystemEntity;
import com.ibm.itim.dataservices.model.domain.DirectorySystemSearch;
import com.ibm.itim.dataservices.model.domain.PersonEntity;
import com.ibm.itim.dataservices.model.domain.PersonSearch;
import com.ibm.itim.dataservices.model.domain.RoleEntity;
import com.ibm.itim.dataservices.model.domain.RoleSearch;
import com.ibm.itim.dataservices.model.domain.ServiceEntity;
import com.ibm.itim.dataservices.model.domain.ServiceSearch;
import com.ibm.itim.policy.analysis.PPAEntitlement;
import com.ibm.itim.policy.analysis.PPAException;
import com.ibm.itim.policy.analysis.PPAProvisioningParameter;
import com.ibm.itim.policy.analysis.PPAProvisioningPolicy;
import com.ibm.itim.policy.analysis.ProvisioningPolicyAnalysis;

/**
 * This class provides an example of how to invoke the provisioning policy
 * analysis APIs.
 * <p>
 * See the Readme.html file contained in this directory for information on how
 * to compile and run the example.
 * 
 * @author dpalmier
 */
public class ProvisioningPolicyAnalysisExample {

	private static final String GET_ENTITLEMENT = "getEntitlement";

	private static final String GET_ENTITLEMENTS = "getEntitlements";

	private static final String GET_PROVISIONING_PARAMETERS = "getProvisioningParameters";

	private static final String GET_PROVISIONING_POLICIES = "getProvisioningPolicies";

	private static final String GET_PROVISIONING_POLICIES_WILDCARD = "getProvisioningPoliciesWildcard";

	private static final String DEFAULT_TENANT_ID = "default.tenant";

	private static final String EXAMPLE_PROPERTIES = "example.properties";

	private static final String PERSON = "person";

	private static final String PERSON_CLASS = "inetOrgPerson";

	private static final String ROLE = "role";

	private static final String ROLE_CLASS = "erRole";

	private static final String SERVICE = "service";

	private static final String SERVICE_CLASS = "erITIMService";

	private Properties properties;

	private DistinguishedName tenantDN;

	private PersonEntity person;

	private ServiceEntity service;

	private RoleEntity role;

	/**
	 * Internal class that hold all of the parameters together.
	 */
	private static class Parameters {

		public boolean doGetEntitlement;

		public boolean doGetEntitlements;

		public boolean doGetProvisioningParameters;

		public boolean doGetProvisioningPolicies;

		public boolean doGetProvisioningPoliciesWildcard;
	}

	/**
	 * The main program that should be invoked to run the provisioning policy
	 * analysis example.
	 * 
	 * @param arg
	 *            The arguments to the program.
	 */
	public static void main(String[] arg) {
		ProvisioningPolicyAnalysisExample example = new ProvisioningPolicyAnalysisExample();
		example.run(arg);
		// This example does not like to exit on its own, so force an exit.
		System.exit(0);
	}

	/**
	 * Constructs a new instance of a provisioning policy analysis example.
	 */
	private ProvisioningPolicyAnalysisExample() {
	}

	/**
	 * Runs the provisioning policy analysis example.
	 * 
	 * @param args
	 *            The arguments to the program.
	 */
	private void run(String[] args) {
		print(0, "Start of example");

		Parameters params = parseArguments(args);

		if (params.doGetEntitlement) {
			getEntitlement(1);
		}

		if (params.doGetEntitlements) {
			getEntitlements(1);
		}

		if (params.doGetProvisioningParameters) {
			getProvisioningParameters(1);
		}

		if (params.doGetProvisioningPolicies) {
			getProvisioningPolicies(1, false);
		}

		if (params.doGetProvisioningPoliciesWildcard) {
			getProvisioningPolicies(1, true);
		}

		print(0, "End of example");
	}

	private Parameters parseArguments(String[] args) {
		Parameters params = new Parameters();

		if (args == null || args.length == 0) {
			// If no arguments, then run all.
			params.doGetEntitlement = true;
			params.doGetEntitlements = true;
			params.doGetProvisioningParameters = true;
			params.doGetProvisioningPolicies = true;
			params.doGetProvisioningPoliciesWildcard = true;
		} else {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.equalsIgnoreCase(GET_ENTITLEMENT)) {
					params.doGetEntitlement = true;
				} else if (arg.equalsIgnoreCase(GET_ENTITLEMENTS)) {
					params.doGetEntitlements = true;
				} else if (arg.equalsIgnoreCase(GET_PROVISIONING_PARAMETERS)) {
					params.doGetProvisioningParameters = true;
				} else if (arg.equalsIgnoreCase(GET_PROVISIONING_POLICIES)) {
					params.doGetProvisioningPolicies = true;
				} else if (arg
						.equalsIgnoreCase(GET_PROVISIONING_POLICIES_WILDCARD)) {
					params.doGetProvisioningPoliciesWildcard = true;
				} else {
					printUsage(arg);
					break;
				}
			}
		}

		return params;
	}

	private void printUsage(String argument) {
		print(1, "Unrecognized argument \"" + argument + "\".");
		print(1, "Valid arguments are:");
		print(2, GET_ENTITLEMENT);
		print(2, GET_ENTITLEMENTS);
		print(2, GET_PROVISIONING_PARAMETERS);
		print(2, GET_PROVISIONING_POLICIES);
		print(2, GET_PROVISIONING_POLICIES_WILDCARD);
	}

	private Properties getProps() {
		if (properties == null) {
			properties = new Properties();

			try {
				properties.load(new FileInputStream(EXAMPLE_PROPERTIES));
			} catch (FileNotFoundException ex) {
				printError(0, "Unable to find properties file: "
						+ ex.getMessage());
				loadDefaultProperties(properties);
			} catch (IOException ex) {
				printError(0, "Caught IOException while loading properties: "
						+ ex.getMessage());
				loadDefaultProperties(properties);
			}
		}

		return properties;
	}

	private void loadDefaultProperties(Properties props) {
		print(0, "Using default properties, and not example.properties file.");
		props.setProperty("person", "System Administrator");
		props.setProperty("role", "Administrators");
		props.setProperty("service", "ITIM Service");
		props.setProperty("default.tenant", "org");
	}

	/**
	 * Gets an entitlement.
	 * 
	 * @param level
	 *            The level at which the entitlement is being obtained.
	 */
	private void getEntitlement(int level) {
		print(level, "Start of getEntitlement");
		person = getPerson(level + 1);
		if (person != null) {
			printPerson(person, level + 1);
		}

		service = getService(level + 1);
		if (service != null) {
			printService(service, level + 1);
			try {
				PPAEntitlement entitlement = ProvisioningPolicyAnalysis
						.getEntitlement(person, service);
				printEntitlement(person, service, entitlement, level + 1);
			} catch (PPAException ppaException) {
				printError(level + 1, "Caught PPAException");
				printError(level + 1, ppaException.toString());
				ppaException.printStackTrace();
			}
		}
		print(level, "End of getEntitlement");
	}

	/**
	 * Gets entitlements.
	 * 
	 * @param level
	 *            The level at which the entitlements are being obtained.
	 */
	private void getEntitlements(int level) {
		print(level, "Start of getEntitlements");
		person = getPerson(level + 1);
		if (person != null) {
			printPerson(person, level + 1);
			try {
				Collection<PPAEntitlement> entitlements = ProvisioningPolicyAnalysis
						.getEntitlements(person);
				printEntitlements(person, entitlements, level + 1);
			} catch (PPAException ppaException) {
				printError(level + 1, "Caught PPAException");
				printError(level + 1, ppaException.toString());
				ppaException.printStackTrace();
			}
		}
		print(level, "End of getEntitlements");
	}

	/**
	 * Gets provisioning parameters.
	 * 
	 * @param level
	 *            The level at which the provisioning parameters are being
	 *            obtained.
	 */
	private void getProvisioningParameters(int level) {
		print(level, "Start of getProvisioningParameters");
		person = getPerson(level + 1);
		if (person != null) {
			printPerson(person, level + 1);
		}

		service = getService(level + 1);
		if (service != null) {
			printService(service, level + 1);
			Vector<String> parameters = new Vector<String>();
			parameters.addElement("eruid");
			parameters.addElement("erpassword");
			printParameters(parameters, level + 1);
			try {
				Collection<PPAProvisioningParameter> provisioningParams = ProvisioningPolicyAnalysis
						.getProvisioningParameters(person, service, parameters);
				printProvisioningParameters(person, service, parameters,
						provisioningParams, level + 1);
			} catch (PPAException ppaException) {
				printError(level + 1, "Caught PPAException");
				printError(level + 1, ppaException.toString());
				ppaException.printStackTrace();
			}
		}
		print(level, "End of getProvisioningParameters");
	}

	/**
	 * Gets provisioning policies.
	 * 
	 * @param level
	 *            The level at which the provisioning policies are being
	 *            obtained.
	 * @param wildcard
	 *            Indicates whether or not wildcard provisioning policies should
	 *            be obtained.
	 */
	private void getProvisioningPolicies(int level, boolean wildcard) {
		if (wildcard) {
			print(level, "Start of getProvisioningPoliciesWildcard");
		} else {
			print(level, "Start of getProvisioningPolicies");
		}

		role = getRole(level + 1);
		if (role != null) {
			printRole(role, level + 1);
			printWildcard(wildcard, level + 1);
			try {
				Collection<PPAProvisioningPolicy> provisioningPolicies = ProvisioningPolicyAnalysis
						.getProvisioningPolicies(role, wildcard);
				printProvisioningPolicies(role, wildcard, provisioningPolicies,
						level + 1);
			} catch (PPAException ppaException) {
				printError(level + 1, "Caught PPAException");
				printError(level + 1, ppaException.toString());
				ppaException.printStackTrace();
			}
		}
		if (wildcard) {
			print(level, "End of getProvisioningPoliciesWildcard");
		} else {
			print(level, "End of getProvisioningPolicies");
		}
	}

	/**
	 * Returns the person.
	 * 
	 * @param level
	 *            The level at which the person is being obtained.
	 * @result The person.
	 */
	private PersonEntity getPerson(int level) {
		print(level, "Start of getPerson");
		if (person == null) {
			try {
				String personName = getProps().getProperty(PERSON);
				if (personName == null) {
					printError(level + 1, "Example properties is missing key "
							+ PERSON + ".");
					return null;
				}

				DistinguishedName tenantDN = getTenantDN(level + 1);
				if (tenantDN == null) {
					printError(level + 1, "Can't determine tenant DN.");
					return null;
				}

				CompoundDN compoundDN = new CompoundDN(tenantDN);
				ObjectProfile objectProfile = getObjectProfile(
						ObjectProfileCategory.PERSON, PERSON_CLASS, level + 1);
				if (objectProfile == null) {
					printError(level + 1, "Can't determine object profile.");
					return null;
				}

				String nameAttribute = objectProfile.getNameAttribute();
				if (nameAttribute == null) {
					printError(level + 1, "Cannot determine name attribute.");
					return null;
				}
				String filter = "(" + nameAttribute + "=" + personName + ")";
				SearchParameters searchParameters = new SearchParameters();
				searchParameters.setScope(SearchParameters.SUBTREE_SCOPE);
				PersonSearch personSearch = new PersonSearch();
				SearchResults searchResults = personSearch.searchByFilter(
						compoundDN, filter, searchParameters);
				if (searchResults == null) {
					printError(level + 1, "Search results are null.");
					return null;
				}
				SearchResultsIterator searchResultsIterator = searchResults
						.iterator();
				if (searchResultsIterator == null) {
					printError(level + 1, "Search results iterator is null.");
				} else {
					while ((searchResultsIterator.hasNext())
							&& (person == null)) {
						person = (PersonEntity) searchResultsIterator.next();
					}
					if (person == null) {
						printError(level + 1, "Cannot find person");
					}
				}
			} catch (ModelCommunicationException modelCommunicationException) {
				printError(level + 1, "Caught ModelCommunicationException");
				printError(level + 1, modelCommunicationException.toString());
				modelCommunicationException.printStackTrace();
			} catch (ObjectNotFoundException objectNotFoundException) {
				printError(level + 1, "Caught ObjectNotFoundException");
				printError(level + 1, objectNotFoundException.toString());
				objectNotFoundException.printStackTrace();
			} catch (PartialResultsException partialResultsException) {
				printError(level + 1, "Caught PartialResultsException");
				printError(level + 1, partialResultsException.toString());
				partialResultsException.printStackTrace();
			}
		}
		print(level, "End of getPerson");
		return person;
	}

	/**
	 * Returns the role.
	 * 
	 * @param level
	 *            The level at which the role is being obtained.
	 * @return The role.
	 */
	private RoleEntity getRole(int level) {
		print(level, "Start of getRole");
		if (role == null) {
			try {

				String roleName = getProps().getProperty(ROLE);
				if (roleName == null) {
					printError(level + 1, "Example properties is missing key "
							+ ROLE + ".");
					return null;
				}

				DistinguishedName tenantDN = getTenantDN(level + 1);
				if (tenantDN == null) {
					printError(level + 1, "Can't determine tenant DN.");
					return null;
				}

				CompoundDN compoundDN = new CompoundDN(tenantDN);
				ObjectProfile objectProfile = getObjectProfile(
						ObjectProfileCategory.ROLE, ROLE_CLASS, level + 1);
				if (objectProfile == null) {
					printError(level + 1, "Can't determine object profile.");
					return null;
				}

				String nameAttribute = objectProfile.getNameAttribute();
				if (nameAttribute == null) {
					printError(level + 1, "Cannot determine name attribute.");
					return null;
				}

				String filter = "(" + nameAttribute + "=" + roleName + ")";
				SearchParameters searchParameters = new SearchParameters();
				searchParameters.setScope(SearchParameters.SUBTREE_SCOPE);
				RoleSearch roleSearch = new RoleSearch();
				SearchResults searchResults = roleSearch.searchByFilter(
						compoundDN, filter, searchParameters);
				if (searchResults == null) {
					printError(level + 1, "Search results are null.");
					return null;
				}

				SearchResultsIterator searchResultsIterator = searchResults
						.iterator();
				if (searchResultsIterator == null) {
					printError(level + 1, "Search results iterator is null.");
				} else {
					while ((searchResultsIterator.hasNext()) && (role == null)) {
						role = (RoleEntity) searchResultsIterator.next();
					}
					if (role == null) {
						printError(level + 1, "Cannot find role");
					}
				}
			} catch (ModelCommunicationException modelCommunicationException) {
				printError(level + 1, "Caught ModelCommunicationException");
				printError(level + 1, modelCommunicationException.toString());
				modelCommunicationException.printStackTrace();
			} catch (ObjectNotFoundException objectNotFoundException) {
				printError(level + 1, "Caught ObjectNotFoundException");
				printError(level + 1, objectNotFoundException.toString());
				objectNotFoundException.printStackTrace();
			} catch (PartialResultsException partialResultsException) {
				printError(level + 1, "Caught PartialResultsException");
				printError(level + 1, partialResultsException.toString());
				partialResultsException.printStackTrace();
			}
		}
		print(level, "End of getRole");
		return role;
	}

	/**
	 * Returns the service.
	 * 
	 * @param level
	 *            The level at which the service is being obtained.
	 * @return The service.
	 */
	private ServiceEntity getService(int level) {
		print(level, "Start of getService");

		if (service == null) {
			try {
				String serviceName = getProps().getProperty(SERVICE);
				if (serviceName == null) {
					printError(level + 1, "Example properties is missing key "
							+ SERVICE + ".");
					return null;
				}

				DistinguishedName tenantDN = getTenantDN(level + 1);
				if (tenantDN == null) {
					printError(level + 1, "Can't determine tenant DN.");
					return null;
				}

				CompoundDN compoundDN = new CompoundDN(tenantDN);
				ObjectProfile objectProfile = getObjectProfile(
						ObjectProfileCategory.SERVICE, SERVICE_CLASS, level + 1);
				if (objectProfile == null) {
					printError(level + 1, "Can't determine object profile.");
					return null;
				}

				String nameAttribute = objectProfile.getNameAttribute();
				if (nameAttribute == null) {
					printError(level + 1, "Cannot determine name attribute.");
					return null;
				}
				String filter = "(" + nameAttribute + "=" + serviceName + ")";
				SearchParameters searchParameters = new SearchParameters();
				searchParameters.setScope(SearchParameters.SUBTREE_SCOPE);
				ServiceSearch serviceSearch = new ServiceSearch();
				SearchResults searchResults = serviceSearch.searchByFilter(
						compoundDN, filter, searchParameters);
				if (searchResults == null) {
					printError(level + 1, "Search results are null.");
					return null;
				}
				SearchResultsIterator searchResultsIterator = searchResults
						.iterator();
				if (searchResultsIterator == null) {
					printError(level + 1, "Search results iterator is null.");
				} else {
					while ((searchResultsIterator.hasNext())
							&& (service == null)) {
						service = (ServiceEntity) searchResultsIterator.next();
					}

					if (service == null) {
						printError(level + 1, "Cannot find service");
					}
				}
			} catch (ModelCommunicationException modelCommunicationException) {
				printError(level + 1, "Caught ModelCommunicationException");
				printError(level + 1, modelCommunicationException.toString());
				modelCommunicationException.printStackTrace();
			} catch (ObjectNotFoundException objectNotFoundException) {
				printError(level + 1, "Caught ObjectNotFoundException");
				printError(level + 1, objectNotFoundException.toString());
				objectNotFoundException.printStackTrace();
			} catch (PartialResultsException partialResultsException) {
				printError(level + 1, "Caught PartialResultsException");
				printError(level + 1, partialResultsException.toString());
				partialResultsException.printStackTrace();
			}
		}
		print(level, "End of getService");
		return service;
	}

	/**
	 * Returns the object profile for the specified category and object class.
	 * 
	 * @param category
	 *            The category of the object profile to be obtained.
	 * @param objectClass
	 *            The class of the object profile to be obtained.
	 * @param level
	 *            The level at which the object profile is being obtained.
	 * @return The object profile for the specified category and object class.
	 */
	private ObjectProfile getObjectProfile(String category, String objectClass,
			int level) {
		print(level, "Start of getObjectProfile");
		ObjectProfile objectProfile = null;

		DistinguishedName tenantDN = getTenantDN(level + 1);
		if (tenantDN != null) {
			Vector<String> objectClasses = new Vector<String>();
			objectClasses.add(objectClass);
			objectProfile = ProfileLocator.getProfileByClass(tenantDN,
					category, objectClasses);
			if (objectProfile == null) {
				printError(level + 1, "Cannot find object profile.");
			}
		}

		print(level, "End of getObjectProfile");
		return objectProfile;
	}

	/**
	 * Returns the tenant distinguished name.
	 * 
	 * @param level
	 *            The level at which the tenant distinguished name is being
	 *            obtained.
	 * @return The tenant distinguished name.
	 */
	private DistinguishedName getTenantDN(int level) {
		print(level, "Start of getTenantDN");
		if (tenantDN == null) {
			try {
				String defaultTenantId = getProps().getProperty(
						DEFAULT_TENANT_ID);
				if (defaultTenantId == null) {
					print(level + 1, "Product properties does not contain key "
							+ DEFAULT_TENANT_ID);
					return null;
				}

				DirectorySystemSearch directorySystemSearch = new DirectorySystemSearch();
				DirectorySystemEntity directorySystemEntity = directorySystemSearch
						.searchById(defaultTenantId);
				if (directorySystemEntity == null) {
					printError(level + 1, "Directory system entity is null.");
				} else {
					tenantDN = directorySystemEntity.getDistinguishedName();
					if (tenantDN == null) {
						printError(level + 1, "Cannot find tenant DN.");
					}
				}
			} catch (ModelException modelException) {
				printError(level + 1, "Caught ModelException");
				printError(level + 1, modelException.toString());
				modelException.printStackTrace();
			}
		}

		print(level, "End of getTenantDN");
		return tenantDN;
	}

	/**
	 * Returns the indentation string for the specified level.
	 * 
	 * @param level
	 *            The level.
	 * @return The indentation string for the specified level.
	 */
	private static final String getIndentationString(int level) {
		String levelIndentString = "  ";
		StringBuilder builder = new StringBuilder(levelIndentString.length()
				* level);

		if (level > 0) {
			for (int i = 0; i < level; i++) {
				builder.append(levelIndentString);
			}
		}

		return builder.toString();
	}

	/**
	 * Prints the specified entitlement.
	 * 
	 * @param entitlement
	 *            The entitlement to be printed.
	 * @param level
	 *            The level at which the entitlement is to be printed.
	 */
	private void printEntitlement(PPAEntitlement entitlement, int level) {
		print(level, "Start of Entitlement");
		if (entitlement == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "targetName         = "
					+ entitlement.getTargetName());
			String targetTypeString = null;
			int targetType = entitlement.getTargetType();
			if (targetType == PPAEntitlement.ALL) {
				targetTypeString = "ALL";
			} else if (targetType == PPAEntitlement.HOST_SELECTION_POLICY) {
				targetTypeString = "HOST_SELECTION_POLICY";
			} else if (targetType == PPAEntitlement.SERVICE_INSTANCE) {
				targetTypeString = "SERVICE_INSTANCE";
			} else if (targetType == PPAEntitlement.SERVICE_TYPE) {
				targetTypeString = "SERVICE_TYPE";
			} else {
				targetTypeString = "UNKNOWN (" + targetType + ")";
			}
			print(level + 1, "targetType         = " + targetTypeString);
			print(level + 1, "Ownership Type     = " + entitlement.getOwnershipType());
			print(level + 1, "isWorkflowRequired = "
					+ entitlement.isWorkflowRequired());
			Collection<PPAProvisioningParameter> provisioningParameters = entitlement
					.getProvisioningParameters();
			printProvisioningParameters(provisioningParameters, level + 1);
		}
		print(level, "End of Entitlement");
	}

	/**
	 * Prints the entitlement for the specified person and service.
	 * 
	 * @param person
	 *            The person to be printed.
	 * @param service
	 *            The service to be printed.
	 * @param entitlement
	 *            The entitlement to be printed.
	 * @param level
	 *            The level at which the entitlement is to be printed.
	 */
	private void printEntitlement(PersonEntity person, ServiceEntity service,
			PPAEntitlement entitlement, int level) {
		print(level, "Start of Entitlement Information");
		printPerson(person, level + 1);
		printService(service, level + 1);
		printEntitlement(entitlement, level + 1);
		print(level, "End of Entitlement Information");
	}

	/**
	 * Prints the specified entitlements.
	 * 
	 * @param entitlements
	 *            The entitlements to be printed.
	 * @param level
	 *            The level at which the entitlements are to be printed.
	 */
	private void printEntitlements(Collection<PPAEntitlement> entitlements,
			int level) {
		print(level, "Start of Entitlements");
		if (entitlements == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "Size = " + entitlements.size());

			for (PPAEntitlement entitlement : entitlements) {
				printEntitlement(entitlement, level + 1);
			}
		}
		print(level, "End of Entitlements");
	}

	/**
	 * Prints the entitlements for the specified person.
	 * 
	 * @param person
	 *            The person to be printed.
	 * @param entitlements
	 *            The entitlements to be printed.
	 * @param level
	 *            The level at which the entitlements are to be printed.
	 */
	private void printEntitlements(PersonEntity person,
			Collection<PPAEntitlement> entitlements, int level) {
		print(level, "Start of Entitlements Information");
		printPerson(person, level + 1);
		printEntitlements(entitlements, level + 1);
		print(level, "End of Entitlements Information");
	}

	/**
	 * Prints the specified parameters.
	 * 
	 * @param parameters
	 *            The parameters to be printed.
	 * @param level
	 *            The level at which the parameters are to be printed.
	 */
	private void printParameters(Collection<String> parameters, int level) {
		print(level, "Start of Parameters");
		if (parameters == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "Size = " + parameters.size());

			for (String parameter : parameters) {
				print(level + 1, parameter);
			}
		}
		print(level, "End of Parameters");
	}

	/**
	 * Prints the specified person.
	 * 
	 * @param person
	 *            The person to be printed.
	 * @param level
	 *            The level at which the person is to be printed.
	 */
	private void printPerson(PersonEntity person, int level) {
		print(level, "Start of Person");
		if (person == null) {
			print(level + 1, "null");
		} else {
			DirectoryObject directoryObject = person.getDirectoryObject();
			if (directoryObject == null) {
				print(level + 1, "Directory Object = null");
			} else {
				print(level + 1, "name = " + directoryObject.getName());
			}
		}
		print(level, "End of Person");
	}

	/**
	 * Prints the specified provisioning parameter.
	 * 
	 * @param provisioningParameter
	 *            The provisioning parameter to be printed.
	 * @param level
	 *            The level at which the provisioning parameter is to be
	 *            printed.
	 */
	private void printProvisioningParameter(
			PPAProvisioningParameter provisioningParameter, int level) {
		print(level, "Start of Provisioning Parameter");
		if (provisioningParameter == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "name = " + provisioningParameter.getName());
			print(level + 1, "values:");
			Object[] values = provisioningParameter.getValues();
			if (values == null) {
				print(level + 2, "null");
			} else {
				print(level + 1, "length = " + values.length);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					if (value == null) {
						print(level + 2, "null");
					} else {
						print(level + 2, value.toString());
					}
				}
			}

			print(level + 1, "enforcements:");
			int[] enforcements = provisioningParameter.getEnforcements();
			if (enforcements == null) {
				print(level + 2, "null");
			} else {
				print(level + 2, "length = " + enforcements.length);
				for (int i = 0; i < enforcements.length; i++) {
					String enforcementString = null;
					int enforcement = enforcements[i];
					if (enforcement == PPAProvisioningParameter.ENFORCEMENT_ALLOWED) {
						enforcementString = "ENFORCEMENT_ALLOWED";
					} else if (enforcement == PPAProvisioningParameter.ENFORCEMENT_DEFAULT) {
						enforcementString = "ENFORCEMENT_DEFAULT";
					} else if (enforcement == PPAProvisioningParameter.ENFORCEMENT_EXCLUDED) {
						enforcementString = "ENFORCEMENT_EXCLUDED";
					} else if (enforcement == PPAProvisioningParameter.ENFORCEMENT_MANDATORY) {
						enforcementString = "ENFORCEMENT_MANDATORY";
					} else {
						enforcementString = "UNKNOWN (" + enforcement + ")";
					}
					print(level + 2, enforcementString);
				}
			}
		}
		print(level, "End of Provisioning Parameter");
	}

	/**
	 * Prints the specified provisioning parameters.
	 * 
	 * @param provisioningParameters
	 *            The provisioning parameters to be printed.
	 * @param level
	 *            The level at which the provisioning parameters are to be
	 *            printed.
	 */
	private void printProvisioningParameters(
			Collection<PPAProvisioningParameter> provisioningParameters,
			int level) {
		print(level, "Start of Provisioning Parameters");
		if (provisioningParameters == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "Size = " + provisioningParameters.size());

			for (PPAProvisioningParameter parameter : provisioningParameters) {
				printProvisioningParameter(parameter, level + 1);
			}
		}
		print(level, "End of Provisioning Parameters");
	}

	/**
	 * Prints the provisioning parameters for the specified person, service, and
	 * parameters.
	 * 
	 * @param person
	 *            The person to be printed.
	 * @param service
	 *            The service to be printed.
	 * @param parameters
	 *            the parameters to be printed.
	 * @param provisioningParameters
	 *            The provisioning parameters to be printed.
	 * @param level
	 *            The level at which the provisioning parameters are to be
	 *            printed.
	 */
	private void printProvisioningParameters(PersonEntity person,
			ServiceEntity service, Collection<String> parameters,
			Collection<PPAProvisioningParameter> provisioningParameters,
			int level) {
		print(level, "Start of Provisioning Parameters Information");
		printPerson(person, level + 1);
		printService(service, level + 1);
		printParameters(parameters, level + 1);
		printProvisioningParameters(provisioningParameters, level + 1);
		print(level, "End of Provisioning Parameters Information");
	}

	/**
	 * Prints the specified provisioning policies.
	 * 
	 * @param provisioningPolicies
	 *            The provisioning policies to be printed.
	 * @param level
	 *            The level at which the provisioning policies are to be
	 *            printed.
	 */
	private void printProvisioningPolicies(
			Collection<PPAProvisioningPolicy> provisioningPolicies, int level) {
		print(level, "Start of Provisioning Policies");

		if (provisioningPolicies == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "Size = " + provisioningPolicies.size());

			for (PPAProvisioningPolicy provisioningPolicy : provisioningPolicies) {
				printProvisioningPolicy(provisioningPolicy, level + 1);
			}

		}
		print(level, "End of Provisioning Policies");
	}

	/**
	 * Prints the provisioning policies for the specified role.
	 * 
	 * @param role
	 *            The role to be printed.
	 * @param wildcard
	 *            The wildcard to be printed.
	 * @param provisioningPolicies
	 *            The provisioning policies to be printed.
	 * @param level
	 *            The level at which the provisioning policies are to be
	 *            printed.
	 */
	private void printProvisioningPolicies(RoleEntity role, boolean wildcard,
			Collection<PPAProvisioningPolicy> provisioningPolicies, int level) {
		print(level, "Start of Provisioning Policies Information");
		printRole(role, level + 1);
		printWildcard(wildcard, level + 1);
		printProvisioningPolicies(provisioningPolicies, level + 1);
		print(level, "End of Provisioning Policies Information");
	}

	/**
	 * Prints the specified provisioning policy.
	 * 
	 * @param provisioningPolicy
	 *            The provisioning policy to be printed.
	 * @param level
	 *            The level at which the provisioning policy is to be printed.
	 */
	private void printProvisioningPolicy(
			PPAProvisioningPolicy provisioningPolicy, int level) {
		print(level, "Start of Provisioning Policy");
		if (provisioningPolicy == null) {
			print(level + 1, "null");
		} else {
			print(level + 1, "name     = " + provisioningPolicy.getName());
			String scopeString = null;
			int scope = provisioningPolicy.getScope();
			if (scope == PPAProvisioningPolicy.SCOPE_SINGLE_LEVEL) {
				scopeString = "SCOPE_SINGLE_LEVEL";
			} else if (scope == PPAProvisioningPolicy.SCOPE_SUBTREE) {
				scopeString = "SCOPE_SUBTREE";
			} else {
				scopeString = "UNKNOWN (" + Integer.toString(scope) + ")";
			}
			print(level + 1, "scope    = " + scopeString);
			print(level + 1, "priority = " + provisioningPolicy.getPriority());

			Collection<PPAEntitlement> entitlements = provisioningPolicy
					.getEntitlements();
			printEntitlements(entitlements, level + 1);
		}
		print(level, "End of Provisioning Policy");
	}

	/**
	 * Prints the specified role.
	 * 
	 * @param role
	 *            The role to be printed.
	 * @param level
	 *            The level at which the role is to be printed.
	 */
	private void printRole(RoleEntity role, int level) {
		print(level, "Start of Role");
		if (role == null) {
			print(level + 1, "null");
		} else {
			DirectoryObject directoryObject = role.getDirectoryObject();
			if (directoryObject == null) {
				print(level + 1, "Directory Object = null");
			} else {
				print(level + 1, "name = " + directoryObject.getName());
			}
		}
		print(level, "End of Role");
	}

	/**
	 * Prints the specified service.
	 * 
	 * @param service
	 *            The service to be printed.
	 * @param level
	 *            The level at which the service is to be printed.
	 */
	private void printService(ServiceEntity service, int level) {
		print(level, "Start of Service");
		if (service == null) {
			print(level + 1, "null");
		} else {
			DirectoryObject directoryObject = service.getDirectoryObject();
			if (directoryObject == null) {
				print(level + 1, "Directory Object = null");
			} else {
				print(level + 1, "name = " + directoryObject.getName());
			}
		}
		print(level, "End of Service");
	}

	/**
	 * Prints the specified wildcard.
	 * 
	 * @param wildcard
	 *            The wildcard to be printed.
	 * @param level
	 *            The level at which the wildcard is to be printed.
	 */
	private void printWildcard(boolean wildcard, int level) {
		print(level, "Wildcard = " + Boolean.toString(wildcard));
	}

	/**
	 * Prints the specified message with the correct indentation for the given
	 * level.
	 * 
	 * @param level
	 *            The number of levels to indent.
	 * @param message
	 *            The message to print.
	 */
	private void print(int level, String message) {
		String indentationString = getIndentationString(level);
		System.out.println(indentationString + message);
	}

	/**
	 * Prints the specified message with the correct indentation for the given
	 * level to the standard error output.
	 * 
	 * @param level
	 *            The number of levels to indent.
	 * @param message
	 *            The message to print.
	 */
	private void printError(int level, String message) {
		String indentationString = getIndentationString(level);
		System.err.println(indentationString + message);
	}

}
