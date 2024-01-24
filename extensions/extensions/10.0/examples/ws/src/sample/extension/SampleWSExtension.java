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
package sample.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;

import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ContainerManager;
import com.ibm.itim.apps.provisioning.GroupManager;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Group;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.ws.client.util.WSSessionExt;
import com.ibm.itim.ws.client.util.xml.XMLBeanReader;
import com.ibm.itim.ws.client.util.xml.XMLBeanWriter;
import com.ibm.itim.ws.model.WSPerson;

public class SampleWSExtension {
	public String GetServiceGroups(WSSessionExt session, String paramsXML) {
		String retVal = "";
		try {
			// Get the Subject and PlatformContext information from the
			// WSSessionExt object
			Subject subject = session.getSubject();
			PlatformContext platform = session.getPlatform();

			// Using XMLBeanReader get the WSPerson object from the XML string
			WSPerson person = (WSPerson) XMLBeanReader.readXMLBean(paramsXML,
					WSPerson.class);
			String ownerDN = person.getItimDN();
			// Construct a filter to get all the services which is owned by the
			// person
			String filter = "(&(objectclass=erServiceItem)(owner=" + ownerDN
					+ "))";
			ContainerManager containerManager = new ContainerManager(platform,
					subject);
			SearchMO searchMO = new SearchMO(platform, subject);
			searchMO.setContext(containerManager.getRoot());
			searchMO.setCategory(ObjectProfileCategory.SERVICE);
			searchMO.setFilter(filter);

			// Execute the search
			Collection services = searchMO.execute().getResults();
			GroupManager grpMgr = new GroupManager(platform, subject);
			Locale locale = Locale.getDefault();

			List<String> result = new ArrayList();
			// Iterate through each of the services returned from the search
			for (Iterator iterator = services.iterator(); iterator.hasNext();) {
				Service service = (Service) iterator.next();
				String servName = service.getName();
				String serviceDN = service.getDistinguishedName().getAsString();
				// Get a ServiceMO object
				ServiceMO serviceMO = new ServiceMO(platform, subject,
						new DistinguishedName(serviceDN));
				// Construct a SearchResultsMO
				SearchResultsMO searchResultMO = new SearchResultsMO(platform,
						subject);
				// Search for all the group objects on the service using the
				// ServiceMO and SearchResultsMO
				grpMgr.getGroups(serviceMO, null, "*", searchResultMO, locale);

				Collection groups = null;
				groups = searchResultMO.getResults();
				// Iterate through all the groups
				for (Iterator grpItr = groups.iterator(); grpItr.hasNext();) {
					Group grp = (Group) grpItr.next();
					String grpName = grp.getName();
					// Prepare a string as "<service_name>|<group_name>" and add
					// it to a list
					result.add(servName + "|" + grpName);
					System.out.println(servName + "|" + grpName);
				}
			}
			// Using XMLBeanWriter convert the list of strings to an XML string
			retVal = XMLBeanWriter.writeXMLBean(result);
		} catch (Exception e) {
			System.out.println("Exception :");
			e.printStackTrace();
		}
		return retVal;
	}

}
