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

import java.rmi.*;
import java.util.*;

import com.ibm.itim.apps.recon.*;
import com.ibm.itim.apps.*;
import com.ibm.itim.apps.provisioning.*;
import com.ibm.itim.apps.exception.AppProcessingException;
import com.ibm.itim.apps.search.*;
import com.ibm.itim.dataservices.model.*;
import com.ibm.itim.dataservices.model.domain.*;

import examples.api.Utils;

import javax.security.auth.*;
import javax.security.auth.login.*;

/**
* Sample command-line Java class to get available recon attributes of a service
*/
public class GetAvailableReconAttributes {

   /**
    * Command line argument names (prefixed by "-")
    */
   private static final String SERVICE_PROFILE  = "serviceprofile";
   private static final String SERVICE_FILTER   = "servicefilter";

   private static final String[][] utilParams = new String[0][0];

   /**
    * main method.
    */
   public static void main(String[] args) {

        Utils utils = new Utils(utilParams, true);
        Hashtable arguments = parseArgs(args);
        PlatformContext platform = null;

        try {
            String tenantId = utils.getProperty(Utils.TENANT_ID);
            String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);

            platform = utils.getPlatformContext();
            Subject subject = utils.getSubject();

            System.out.println("Searching for Service \n");
            // Use the Search API to locate the the Service instance
            SearchMO searchMO = new SearchMO(platform, subject);
            searchMO.setContext(new CompoundDN(new 
                      DistinguishedName("ou="+tenantId+"," + ldapServerRoot)));
            searchMO.setCategory(ObjectProfileCategory.SERVICE);
            searchMO.setFilter((String)arguments.get(SERVICE_FILTER));
            searchMO.setProfileName((String)arguments.get(SERVICE_PROFILE));
            SearchResultsMO searchResultsMO = null;
            Collection services = null;
            try {
                searchResultsMO = searchMO.execute();
                services = searchResultsMO.getResults();
                if(services == null || services.isEmpty()) {

                    System.out.println("Unable to find Service.\n");
                    System.exit(0);
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

            Service service = (Service)services.iterator().next();
            ServiceMO serviceMO =
               new ServiceMO(platform, subject, service.getDistinguishedName());

            System.out.println("Creating ReconManager \n");
            ReconManager reconManager = new ReconManager(platform, subject);

            System.out.println("Querying recon manager to get available attributes..");
            Collection attributes = 
               reconManager.getAvailableReconciliationAttributes(serviceMO);
            if(attributes != null) {
                System.out.println("Available attributes for reconciliation are - ");
                int n = 1;
                Iterator it = attributes.iterator();
                while (it.hasNext()) { 
                    System.out.println("\n" + n++ + "."  + it.next().toString());
                }
            } else {
                System.out.println("No attributes found.");
            }

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
       } catch (AppProcessingException e) {
           e.printStackTrace();
       } finally {
           if(platform != null) {
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
       for (int i=0;i<args.length;i++) {
           argumentList += args[i];
       }
       try {
           StringTokenizer tokenizer =
               new StringTokenizer(argumentList, "-");
           while (tokenizer.hasMoreTokens()) {
               String token = (String)tokenizer.nextToken();
               int delim = token.indexOf("?");
               String name = token.substring(0, delim);
               String value = token.substring(delim+1, token.length());
               if (arguments.get(name) != null) {
                   // arg name used previous
                   Object vals = arguments.get(name);
                   if (vals instanceof String) {
                       // convert to String[]
                       Vector<String> values = new Vector<String>(1);
                       values.add((String)vals);
                       values.add(value);
                       arguments.put(name, values);
                   } else if (vals instanceof Vector) {
                       // add new element to String[]
                       Vector values = (Vector)vals;
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
       if (!arguments.containsKey(SERVICE_PROFILE))
           return printUsage("No Service profile specified");
       if (!arguments.containsKey(SERVICE_FILTER))
           return printUsage("No servicefilter specified");
       return true;
   }

   public static boolean printUsage(String msg) {
       StringBuffer usage = new StringBuffer();
       usage.append("\nGetAvailableReconAttributes: " + msg + "\n");
       usage.append("usage: GetAvailableReconAttributes -[argument] ? \"[value]\"\n");
       usage.append("\n");
       usage.append("-serviceprofile  Object Profile Name for the Service\n");
       usage.append("-servicefilter   Ldap Filter to search for a Service instance ");
       usage.append("\n\nExample: getAvailableReconAttributes ");
       usage.append(" -serviceprofile?WinLocalProfile  -servicefilter?\"(erservicename=WinLocal)\"");
       System.out.println(usage);
       return false;
   }

}
