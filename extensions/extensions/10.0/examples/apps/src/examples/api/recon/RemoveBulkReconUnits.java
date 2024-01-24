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

package examples.api.recon;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.recon.ReconManager;
import com.ibm.itim.apps.recon.ReconUnitData;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;

import examples.api.Utils;

/**
 * Sample command-line Java class to delete all recon units of each service.
 */
public class RemoveBulkReconUnits {

    private static final String[][] utilParams = new String[0][0];

    private static final String ITIMSERVICE = "ITIMService";

    private static final String HOSTEDSERVICE = "HostedService";

    /**
     * main method.
     */
    public static void main(String[] args) {

        Utils utils = new Utils(utilParams, true);
        SearchResultsMO searchResultMO = null;
        PlatformContext platform = null;

        try {
            String tenantId = utils.getProperty(Utils.TENANT_ID);
            String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);

            platform = utils.getPlatformContext();
            Subject subject = utils.getSubject();

            System.out.println("Searching for Services\n");
            // Use the Search API to locate the the Service instance
            SearchMO searchMO = new SearchMO(platform, subject);
            searchMO.setContext(new CompoundDN(new DistinguishedName("ou="
                    + tenantId + "," + ldapServerRoot)));
            searchMO.setCategory(ObjectProfileCategory.SERVICE);
            searchResultMO = searchMO.execute();
            Collection services = searchResultMO.getResults();
            if (services == null || services.isEmpty()) {
                System.out.println("Unable to find any Service.\n");
                System.exit(0);
            }

            System.out.println("Creating ReconManager\n");
            ReconManager reconManager = new ReconManager(platform, subject);

            int serviceNumber = 0;
            Iterator iter = services.iterator();
            while (iter.hasNext()) {
                Service service = (Service) iter.next();
                String serviceName = (String) service.getName();
                String profileName = (String) service.getProfileName();
                // Skip ITIM and Hosted services.
                if (ITIMSERVICE.equals(profileName)
                        || HOSTEDSERVICE.equals(profileName)) {
                    continue;
                }

                ServiceMO serviceMO = new ServiceMO(platform, subject, 
                        service.getDistinguishedName());

                Collection reconUnitsData = reconManager.getReconUnits(serviceMO);

                ReconUnitData deleteReconUnitData = null;
                System.out.println("\nRemoving reconciliation unit for service "
                        + serviceName);
                for (Iterator it = reconUnitsData.iterator(); it.hasNext();) {
                    deleteReconUnitData = (ReconUnitData) it.next();
                    String reconUnitName = deleteReconUnitData.getName();
                    reconManager.removeReconUnitData(serviceMO,
                            deleteReconUnitData);
                    System.out.println("Removed '" + reconUnitName
                            + "' reconciliation unit successfully.");
                    serviceNumber++;
                }
            } //End of service iteration.

            System.out.println("\nDeletion of Recon Units completed successfully \n");
            System.out.println("Total number of reconciliation unit deleted : "
                            + serviceNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
        } catch (RemoteServicesException e) {
            e.printStackTrace();
        } finally {
            if (searchResultMO != null) {
                try {
                    searchResultMO.close();
                } catch (Exception e) {
                    // Ignore
                }
                searchResultMO = null;
            }
            if (platform != null) {
                platform.close();
                platform = null;
            }
        }
    }

}
