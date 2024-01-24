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
import java.util.Collection;
import java.util.regex.Pattern;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ModelCreationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.domain.BusinessUnitFactory;
import com.ibm.itim.dataservices.model.domain.BusinessUnit;
import com.ibm.itim.dataservices.model.domain.OrganizationalContainerEntity;
import com.ibm.itim.dataservices.model.domain.OrganizationalContainerSearch;

/**
 * This is an example class to demonstrate bulk loading of organization chart data into
 * Tivoli Identity Manager.  The entry point to the program is the main() method, which
 * can be executed from the command line
 * 
 * > java examples.dataservices.BusinessUnitLoader
 * 
 * The environment, including classpath must be set up as in the other data services examples.
 * 
 * The program reads the configuration file bulk_org_chart.properties
 * in the current working directory for the organization, delimiter and data file.
 */
public class BusinessUnitLoader {
	/**
	 * The configuration file for the program
	 */
	public static String CONFIG_FILE = "bulk_org_chart.properties";

	private Pattern pattern;
	private OrganizationalContainerSearch search = new OrganizationalContainerSearch();
	private BusinessUnitFactory businessUnitFactory = new BusinessUnitFactory();
	private ApplicationSettings applicationSettings = new PropertiesFileApplicationSettings(CONFIG_FILE);
	
	/**
	 * Loads the business units in the configuration file into the TIM data store.
	 * @throws FileNotFoundException If the business unit data file cannot be found
	 * @throws IOException If the file could not be read
	 * @throws ModelCommunicationException If the program cannot connect to the TIM data store
	 * @throws ObjectNotFoundException If the top level organization cannot be found
	 */
	public void load() throws FileNotFoundException, IOException, 
			ModelCommunicationException, ObjectNotFoundException {
		applicationSettings.load();
		pattern = Pattern.compile(applicationSettings.getDelimiter());
		System.out.println("Loading business units from CSV file " + applicationSettings.getDataFileName());
		DistinguishedName orgDN = new DistinguishedName(applicationSettings.getOrganizationDN());
		OrganizationalContainerEntity org = search.lookup(orgDN);
		BufferedReader reader = new BufferedReader(new FileReader(applicationSettings.getDataFileName()));
		String line = reader.readLine();
		while (line != null) {
			if (line.charAt(0) != '#') {  // Skip comments
				addBusinessUnit(line, org);
			}
			line = reader.readLine();
		}
		System.out.println("Finished business units");
	}
	
	private void addBusinessUnit(String line, OrganizationalContainerEntity org) 
			throws ModelCommunicationException {
		String[] tokens = pattern.split(line);
		System.out.println("Loading business unit " + tokens[0] + "\n");
		AttributeValue name = new AttributeValue("ou", tokens[0]);
		AttributeValue description = new AttributeValue("description", tokens[1]);
		BusinessUnit businessUnit = new BusinessUnit();
		businessUnit.setAttribute(name);
		businessUnit.setAttribute(description);
		String parentName = null;
		if (tokens.length > 2) {
			parentName = tokens[2];
		}
		OrganizationalContainerEntity parent = findParent(parentName, org);
		try {
			businessUnitFactory.create(parent, businessUnit);
		} catch(ModelCreationException e) {
			System.err.println("Could not create business unit " + parentName + ". Reason: " +
					e.getMessage());
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
				Collection collection = results.toCollection();
				if (collection.size() == 1) {
					parent = (OrganizationalContainerEntity)collection.iterator().next();
				} else {
					System.err.println("Could not find a unique match for business unit " +
							parentName + ". Found " + collection.size());
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
	
	public static void main(String[] argv) {
		BusinessUnitLoader businessUnitLoader = new BusinessUnitLoader();
		
		try {
			businessUnitLoader.load();
		} catch(FileNotFoundException e) {
			System.err.print("Could not find file: " + e.getMessage());
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
