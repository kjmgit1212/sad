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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValueIterator;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ModelCreationException;
import com.ibm.itim.dataservices.model.ModelIntegrityException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.domain.OrganizationalContainerEntity;
import com.ibm.itim.dataservices.model.domain.OrganizationalContainerSearch;
import com.ibm.itim.dataservices.model.domain.PersonEntity;
import com.ibm.itim.dataservices.model.domain.PersonSearch;
import com.ibm.itim.dataservices.model.domain.Role;
import com.ibm.itim.dataservices.model.domain.RoleEntity;
import com.ibm.itim.dataservices.model.domain.RoleFactory;
import com.ibm.itim.dataservices.model.domain.RoleSearch;

/**
 * This is an example class to demonstrate bulk loading of organizational role data into
 * Tivoli Identity Manager.  The entry point to the program is the main() method, which
 * can be executed from the command line
 * 
 * > java examples.dataservices.RoleLoader
 * 
 * The environment, including classpath must be set up as in the other data services examples.
 * 
 * The program reads the configuration file bulk_role.properties
 * in the current working directory for the organization, delimiter and data file.
 */
public class RoleLoader {

	private OrganizationalContainerSearch search = new OrganizationalContainerSearch();
	private RoleSearch roleSearch = new RoleSearch();
	private PersonSearch personSearch = new PersonSearch();
	private RoleFactory roleFactory = new RoleFactory();
	private Pattern pattern;
	private ApplicationSettings applicationSettings = new PropertiesFileApplicationSettings();
	
	/**
	 * Loads the roles in the configuration file into the TIM data store.
	 * @throws FileNotFoundException If the role data file cannot be found
	 * @throws IOException If the file could not be read
	 * @throws ModelCommunicationException If the program cannot connect to the TIM data store
	 * @throws ObjectNotFoundException If the top level organization cannot be found
	 */
	public void load() throws FileNotFoundException, IOException,
			ModelCommunicationException, ObjectNotFoundException {
		applicationSettings.load();
		pattern = Pattern.compile(applicationSettings.getDelimiter());
		Date start = new Date();
		System.out.println("Loading roles from CSV file " + applicationSettings.getDataFileName());
		DistinguishedName orgDN = new DistinguishedName(applicationSettings.getOrganizationDN());
		OrganizationalContainerEntity org = search.lookup(orgDN);
		BufferedReader reader = new BufferedReader(new FileReader(applicationSettings.getDataFileName()));
		String line = reader.readLine();
		while (line != null) {
			if (line.charAt(0) != '#') {  // Skip comments
				addRole(line, org);
			}
			line = reader.readLine();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
		System.out.println("Started loading roles at " + sdf.format(start) + 
				". Finished at " + sdf.format(new Date()));
	}
	
	// Parse a single line of the data file and add the role found to the TIM data store
	private void addRole(String line, OrganizationalContainerEntity org) 
			throws ModelCommunicationException{
		String[] tokens = pattern.split(line);
		String roleName =  null;
		if (tokens.length > 0) {
			roleName =  tokens[0];
		}
		System.out.println("Loading role " + tokens[0] + "\n");
		AttributeValues attributes = new AttributeValues();
		
		// First token is the name of the role
		if ((roleName == null) || (roleName.trim().length() == 0)) {
			System.out.println("Role name is empty.\n");
			return;
		}
		AttributeValue name = new AttributeValue(Role.ROLE_ATTR_NAME, roleName);
		attributes.put(name);
		
		// Second token is the role description
		if ((tokens.length > 1) && (tokens[1] != null) && (tokens[1].trim().length() > 0)) {
			AttributeValue description = new AttributeValue(Role.ROLE_ATTR_DESCRIPTION, tokens[1]);
			attributes.put(description);
		}
		
		// Third token is the business unit
		OrganizationalContainerEntity parent = org; 
		if ((tokens.length > 2) && (tokens[2] != null) && (tokens[2].trim().length() > 0)) {
			parent = findParent(tokens[2].trim(), org);
		}
		
		// Forth token is the owner
		if ((tokens.length > 3) && (tokens[3] != null) && (tokens[3].trim().length() > 0)) {
			PersonEntity owner = findPerson(tokens[3].trim(), org);
			if (owner != null) {
				AttributeValue ownerAttr = new AttributeValue(Role.OWNER, owner.getDistinguishedName().toString());
				attributes.put(ownerAttr);
			}
		}
		
		// Rest of  the parent
		Collection<RoleEntity> parentRoles = new ArrayList<RoleEntity>();
		for (int i=4; i<tokens.length; i++) {
			String parentRoleName = tokens[i];
			if ((parentRoleName != null) && (parentRoleName.trim().length() > 0)) {
				RoleEntity parentRoleEntity = findRole(parentRoleName.trim(), org);
				if (parentRoleEntity != null) {
					System.out.println("Found parent " + parentRoleName + " for role " + roleName);
					parentRoles.add(parentRoleEntity);
				}
			}
		}
		
		try {
			RoleEntity roleEntity = findRole(roleName, org);
			
			// Create a new role
			if (roleEntity == null) {
				Role role = new Role(attributes);
				roleEntity = roleFactory.create(parent, role);
				
			// Update an existing role
			} else {
				Role role = (Role)roleEntity.getDirectoryObject();
				AttributeValueIterator it = attributes.iterator();
				while (it.hasNext()) {
					AttributeValue av = it.next();
					role.setAttribute(av);
				}
				roleEntity.update();
			}
			
			// Set parent role
			for (RoleEntity parentRoleEntity : parentRoles) {
				System.out.println("Adding member role " + roleName + " to parent " + parentRoleEntity);
				parentRoleEntity.addMemberRole(roleEntity);
			}
		} catch(ModelCreationException e) {
			System.err.println("Could not create role " + roleName + ". Reason: " + e.getMessage());
		} catch(ModelIntegrityException e) {
			System.err.println("Could not create role " + roleName + ". Reason: " + e.getMessage());
		} catch(ObjectNotFoundException e) {
			System.err.println("Could not update role " + roleName+ ". Reason: " + e.getMessage());
		} 
	}
	
	/**
	 * Finds the business unit entity with the given name 
	 * @param parentName The name of the business unit
	 * @param org The top level organization
	 * @return An entity object for the business unit or the top level org if it could not be found
	 * @throws ModelCommunicationException
	 */
	private OrganizationalContainerEntity findParent(String parentName, 
			OrganizationalContainerEntity org) throws ModelCommunicationException {
		OrganizationalContainerEntity parent = org; 
		if ((parentName != null) && (parentName.trim().length() > 0)) {
			String filter = "(ou=" + parentName + ")";
			SearchParameters params = new SearchParameters();
			params.setScope(SearchParameters.SUBTREE_SCOPE);
			try {
				SearchResults results = search.searchByFilter(org, filter, params);
				//SearchResults results = businessUnitSearch.searchByFilter(org, 
				//		"OrganizationalUnit", filter, params);
				Collection collection = results.toCollection();
				if (collection.size() == 1) {
					parent = (OrganizationalContainerEntity)collection.iterator().next();
				} else {
					System.err.println("Could not find a unique match for business unit '" +
							parentName + "'. Found " + collection.size());
				}
			} catch(ObjectNotFoundException e) {
				System.err.println("Error searching for " + parentName + ". Reason: " + 
						e.getMessage());
			} catch(PartialResultsException e) {
				System.err.println("Error searching for " + parentName + ". Reason: " + 
						e.getMessage());
			}
		}
		return parent;
	}
	
	/**
	 * Find the role with the given name
	 * @param roleName The name of the role
	 * @param org The top level organization
	 * @return The first role entity found or null if none found
	 * @throws ModelCommunicationException
	 */
	private RoleEntity findRole(String roleName, 
			OrganizationalContainerEntity org) throws ModelCommunicationException {
		RoleEntity roleEntity = null; 
		if ((roleName != null) && (roleName.trim().length() > 0)) {
			String filter = "(errolename=" + roleName + ")";
			SearchParameters params = new SearchParameters();
			params.setScope(SearchParameters.SUBTREE_SCOPE);
			try {
				SearchResults results = roleSearch.searchByFilter(org, filter, params);
				Collection collection = results.toCollection();
				if (collection.size() > 0) {
					roleEntity = (RoleEntity)collection.iterator().next();
				} else {
					System.err.println("Did not find a match for role " +
							roleName + ". Found " + collection.size());
				}
			} catch(ObjectNotFoundException e) {
				System.err.println("Error searching for " + roleName + ". Reason: " + 
						e.getMessage());
			} catch(PartialResultsException e) {
				System.err.println("Error searching for " + roleName + ". Reason: " + 
						e.getMessage());
			}
		}
		return roleEntity;
	}
	
	/**
	 * Find the person with the common name provided
	 * @param cn The common name of the person
	 * @param org The top level organization
	 * @return The first person entity found or null if none found
	 * @throws ModelCommunicationException
	 */
	private PersonEntity findPerson(String cn, OrganizationalContainerEntity org) 
			throws ModelCommunicationException {
		PersonEntity personEntity = null; 
		if ((cn != null) && (cn.trim().length() > 0)) {
			String filter = "(cn=" + cn + ")";
			SearchParameters params = new SearchParameters();
			params.setScope(SearchParameters.SUBTREE_SCOPE);
			try {
				SearchResults results = personSearch.searchByFilter(org, filter, params);
				Collection collection = results.toCollection();
				if (collection.size() > 0) {
					personEntity = (PersonEntity)collection.iterator().next();
				} else {
					System.err.println("Could not find a match for person '" +
							cn + "'. Found " + collection.size());
				}
			} catch(ObjectNotFoundException e) {
				System.err.println("Error searching for person '" + cn + "'. Reason: " + 
						e.getMessage());
			} catch(PartialResultsException e) {
				System.err.println("Error searching for person '" + cn + "'. Reason: " + 
						e.getMessage());
			}
		}
		return personEntity;
	}
	
	/**
	 * The entry point for the program
	 * @param argv No command line arguments are used
	 */
	public static void main(String[] argv) {
		RoleLoader roleLoader = new RoleLoader();
		
		try {
			roleLoader.load();
		} catch(FileNotFoundException e) {
			System.err.print("Could not find file " + e.getMessage());
			System.exit(-1);
		} catch(IOException e) {
			System.err.print("IO Exception " + e.getMessage());
			System.exit(-1);
		} catch(ModelCommunicationException e) {
			System.err.print("Directory Communication Exception " + e.getMessage());
			System.exit(-1);
		} catch(ObjectNotFoundException e) {
			System.err.print("Could not find the top level organization: " + e.getMessage());
			System.exit(-1);
		}
	}
	
}
