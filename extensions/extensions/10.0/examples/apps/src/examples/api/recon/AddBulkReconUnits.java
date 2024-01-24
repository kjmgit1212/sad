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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.exception.AppProcessingException;
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
 * Sample command-line Java class to add one recon unit to each service for
 * which already recon unit is not present.
 */
public class AddBulkReconUnits {

    /**
     * Command line argument names (prefixed by "-")
     */

    private static final String HOUR = "hour";

    private static final String MINUTE = "minute";

    private static final String MAX_DURATION = "maxduration";

    private static final String LOCK_SERVICE = "lockservice";

    private static final String DESCRIPTION = "description";

    private static final String INCREMENT = "increment";

    private static final String RECON_IDENTIFIER = "reconidentifier";

    private static final String[][] utilParams = new String[0][0];

    private static final String ITIMSERVICE = "ITIMService";

    private static final String HOSTEDSERVICE = "HostedService";
    
    private static final String QUERY_MODE    = "querymode";

    /**
     * main method.
     */
    public static void main(String[] args) {

        Utils utils = new Utils(utilParams, true);
        Hashtable arguments = parseArgs(args);
        SearchResultsMO searchResultMO = null;
        PlatformContext platform = null;

        try {
            String tenantId = utils.getProperty(Utils.TENANT_ID);
            String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);

            platform = utils.getPlatformContext();
            Subject subject = utils.getSubject();

            // Get Recon Identifier
            String reconIdentifierStr = (String) arguments.get(RECON_IDENTIFIER);
            if(reconIdentifierStr == null) {
            	reconIdentifierStr = " Recon Unit";
            } else {
            	reconIdentifierStr = " Recon Unit " + reconIdentifierStr;
            }

            // Get Description
            String description = (String) arguments.get(DESCRIPTION);

            // Get Increment
            int increment = -1;
            String incrementCount = (String) arguments.get(INCREMENT);
            if (incrementCount != null && incrementCount.trim().length() > 0) {
                try {
                    increment = Integer.parseInt((String) arguments.get(INCREMENT));
                } catch (NumberFormatException nfe) {
                    //ignore as increment is already initialize to -1.
                }
            }
            if (increment < 0) {
                System.out.println("Increment not specified or if specified, it is invalid. "
                        + "Valid values : greater than or equal to 0.");
                System.out.println("Using increment count as 1 (default).\n");
                increment = 1;
            }

            // Get Hour and Minute
            int hour = -1;
            try {
                hour = Integer.parseInt((String) arguments.get(HOUR));
            } catch (NumberFormatException nfe) {
                //ignore as hour is already initialize to -1.
            }
            if (hour < 0 || hour > 23) {
                System.out.println("Invalid hour specified. "
                        + "Valid values : 0-23.\n");
                System.exit(0);
            }

            int minute = -1;
            try {
                minute = Integer.parseInt((String) arguments.get(MINUTE));
            } catch (NumberFormatException nfe) {
                //ignore as minute is already initialize to -1.
            }
            if (minute < 0 || minute > 59) {
                System.out.println("Invalid minute specified. "
                        + "Valid values : 0-59.\n");
                System.exit(0);
            }

            // Get max duration of reconciliation
            long maxDuration = 0;
            String strMaxDuration = (String) arguments.get(MAX_DURATION);
            if (strMaxDuration != null && strMaxDuration.trim().length() > 0) {
                try {
                    maxDuration = Long.valueOf(strMaxDuration).longValue();
                } catch (NumberFormatException nfe) {
                }
            }
            if (maxDuration < 1) {
                System.out.println("Max duration not specified or if specified, it is invalid. "
                        + "Valid values : greater than or equal to 1.");
                System.out.println("Using max duration as 600 (default).\n");
                maxDuration = 600;
            }

            // Get lock-service setting
            boolean isLockService = true;
            String lockService = (String) arguments.get(LOCK_SERVICE);
            if (lockService != null
                    && (lockService.equalsIgnoreCase("false") || 
                        lockService.equalsIgnoreCase("true"))) {
                isLockService = Boolean.valueOf(lockService).booleanValue();
            }
            
            // Set recon query mode of reconciliation
            int queryMode = 0;
            String reconQueryMode = (String)arguments.get(QUERY_MODE);
            if(reconQueryMode != null) {
                queryMode = Integer.parseInt(reconQueryMode);
                if(queryMode != 0 && queryMode != 1) {
                    System.out.println("Invalid query mode specified. " + 
                        "Valid values : 0 or 1.\n");
                    System.exit(0);
                }
            }
            
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
                String profileName = (String) service.getProfileName();
                // Skip ITIM and Hosted services.
                if (ITIMSERVICE.equals(profileName) || 
                        HOSTEDSERVICE.equals(profileName)) {
                    continue;
                }

                ServiceMO serviceMO = new ServiceMO(platform, subject, service
                        .getDistinguishedName());

                // Check if already reconciliation unit is defined for current service.
                Collection existingReconUnits = 
                    reconManager.getReconUnits(serviceMO);
                if(existingReconUnits.size() > 0) {
                    continue;
                }

                // Initialize temporary reconciliation unit
                ReconUnitData newReconUnitData = new ReconUnitData();

                // Set Name
                String name = (String) service.getName() + reconIdentifierStr; 
                newReconUnitData.setName(name);

                // Set Description
                if (description != null && description.trim().length() > 0) {
                    newReconUnitData.setDescription(description);
                }

                // As daily Recon to be scheduled set month=0, day of month=-1
                newReconUnitData.setMonth(0);
                newReconUnitData.setDayOfMonth(-1);

                newReconUnitData.setHour(hour);
                newReconUnitData.setMinute(minute);

                // Set max duration of reconciliation
                newReconUnitData.setMaxDuration(maxDuration);

                // Set lock-service setting
                newReconUnitData.setLockService(isLockService);
                
                //Set query mode
                newReconUnitData.setReconQueryMode(queryMode);

                // Set reconciliation attributes to be included.
                Collection attributes = reconManager
                        .getAvailableReconciliationAttributes(serviceMO);
                ArrayList tobeReconciledAttributes = new ArrayList(0);
                tobeReconciledAttributes.addAll(attributes);
                newReconUnitData.setReconQueryAttributes(tobeReconciledAttributes);

                System.out.println("Adding the recon unit...");
                reconManager.addReconUnitData(serviceMO, newReconUnitData, null);

                System.out.println("Added recon unit successfully." +
                		" Name - '"  + name + 
                		"' Details - " + newReconUnitData.toString() + "\n");
                serviceNumber++;
                // Increament Minutes and Hour.
                minute = minute + increment;
                if (minute > 59) {
                    minute = minute - 60;
                    hour = hour + 1;
                    if (hour > 23) {
                        hour = hour - 24;
                    }
                }
            } //End of service iteration.

            System.out.println("Addition of Recon Units completed successfully. \n");
            System.out.println("Total number of services for which recon is scheduled : "
                            + serviceNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
        } catch (AppProcessingException e) {
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

    /**
     * Parses the argument list from the command-line
     */
    public static Hashtable parseArgs(String[] args) {
        Hashtable<String, Object> arguments = new Hashtable<String, Object>();
        String argumentList = "";
        for (int i = 0; i < args.length; i++) {
            argumentList += args[i];
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(argumentList, "-");
            while (tokenizer.hasMoreTokens()) {
                String token = (String) tokenizer.nextToken();
                int delim = token.indexOf("?");
                String name = token.substring(0, delim);
                String value = token.substring(delim + 1, token.length());
                if (arguments.get(name) != null) {
                    // arg name used previous
                    Object vals = arguments.get(name);
                    if (vals instanceof String) {
                        // convert to String[]
                        Vector<String> values = new Vector<String>(1);
                        values.add((String) vals);
                        values.add(value);
                        arguments.put(name, values);
                    } else if (vals instanceof Vector) {
                        // add new element to String[]
                        Vector<String> values = (Vector) vals;
                        values.add(value);
                        arguments.put(name, vals);
                    }
                } else {
                    arguments.put(name, value);
                }
            }
            if (!checkArguments(arguments))
                System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            printUsage("Invalid command syntax");
            System.exit(0);
        }
        return arguments;
    }

    public static boolean checkArguments(Hashtable arguments) {
        if (!arguments.containsKey(HOUR))
            return printUsage("Hour not specified");
        if (!arguments.containsKey(MINUTE))
            return printUsage("Minute not specified");
        return true;
    }

    public static boolean printUsage(String msg) {
        StringBuffer usage = new StringBuffer();
        usage.append("\naddBulkReconUnits: " + msg + "\n");
        usage.append("\nusage: addBulkReconUnits -[argument] ? \"[value]\"\n\n");
        usage.append("-hour                Hour of recon. Valid values 0-23.\n\n");
        usage.append("-minute              Minute of recon. Valid values 0-59.\n\n");
        usage.append("-[reconidentifier]   The unique identifier for recon units to be added.\n\n");
        usage.append("-[increment]         Increment in minutes for the next service recon schedule.\n");
        usage.append("                     valid values - greater than or equal to 0.\n");
        usage.append("                     defaul value = \"1\" \n");
        usage.append("                     value = \"0\" - all added Services ReconUnit scheduled at same time \n\n");
        usage.append("-[description]       Name of description.\n\n");
        usage.append("-[querymode]         0 for normal recon, 1 for supporting data only recon.\n");
        usage.append("-[lockservice]       Specifies if the Service needs to be locked during recon,\n");
        usage.append("                     default value = \"true\"\n\n");
        usage.append("-[maxduration]       Maximum duration(in minutes) for recon process to run.\n");
        usage.append("                     default value = \"600\" \n");
        usage.append("                     minimum value = \"1\".\n\n");
        usage.append("Arguments enclosed in [] are optional\n\n");
        usage.append("Example: addBulkReconUnits -description?\"Recon unit from bulk Add\"");
        usage.append(" -querymode?0 -maxduration?600 -hour?0 -minute?0 -increment?1");
        System.out.println(usage);
        return false;
    }

}
