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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
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
 * Sample command-line Java class to remove reconciliation.unit for
 * a resource.
 */
public class RemoveReconUnit {

   /**
    * Command line argument names (prefixed by "-")
    */

   private static final String SERVICE_PROFILE    = "serviceprofile";
   private static final String SERVICE_FILTER     = "servicefilter";
   private static final String MONTH              = "month";
   private static final String DAY                = "day";
   private static final String DAY_OF_WEEK        = "dayofweek";
   private static final String HOUR               = "hour";
   private static final String MINUTE             = "minute";

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

            
            // Initialize temporary reconciliation unit
            ReconUnitData deleteReconUnitData = new ReconUnitData();

            // Set month
            int month = Integer.parseInt((String)arguments.get(MONTH));
            if(month < -1 || month > 12) {
                System.out.println("Invalid month specified. " + 
                    "Valid values : 1-12, -1 stands for Monthly recon and 0 to ignore.\n");
                System.exit(0);
            }
            deleteReconUnitData.setMonth(month);

            // Set day of month
            if((String)arguments.get(DAY) != null) {
                int day = Integer.parseInt((String)arguments.get(DAY));
                if(day < -1 || day > 31) {
                    System.out.println("Invalid day specified. " + 
                        "Valid values : 1-31, -1 stands for Daily recon and 0 to ignore.\n");
                    System.exit(0);
                }
                deleteReconUnitData.setDayOfMonth(day);
            }

            //Set day of week
            //Should be set only if the day of month is not required
            if((String)arguments.get(DAY) == null && 
               (String)arguments.get(DAY_OF_WEEK) != null) {
                int dayOfWeek = Integer.parseInt(
                                       (String)arguments.get(DAY_OF_WEEK));
                if(dayOfWeek < 0 || dayOfWeek > 7) {
                    System.out.println("Invalid day of week specified. " + 
                        "Valid values 1-7 (1 = Sunday, 2 = Monday, ...) and 0 to ignore.\n");
                    System.exit(0);
                }
                deleteReconUnitData.setDayOfWeek(dayOfWeek);
            }

            // Set hour
            int hour  = Integer.parseInt((String)arguments.get(HOUR));
            if(hour < -1 || hour > 23) {
                System.out.println("Invalid hour specified. " + 
                    "Valid values : 0-23 and -1 stands for Hourly recon.\n");
                System.exit(0);
            }            
            deleteReconUnitData.setHour(hour);

            // Set minute
            int minute = Integer.parseInt((String)arguments.get(MINUTE));
            if(minute < 0 || minute > 59) {
                System.out.println("Invalid minute specified. " + 
                    "Valid values : 0-59.\n");
                System.exit(0);
            }
            deleteReconUnitData.setMinute(minute);

            //Only schedule information is required to identify the 
            //reconciliation unit to be deleted.
            System.out.println("\nRemoving reconciliation unit..");
            reconManager.removeReconUnitData(serviceMO, deleteReconUnitData);
            System.out.println("\nRemoved reconciliation unit successfully.");

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
       } catch (NumberFormatException e) {
           System.out.println("\nInvalid number format, enter valid numbers\n");
       } catch (RemoteServicesException e) {
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
       Hashtable<String,Object> arguments = new Hashtable<String,Object>();
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
           return printUsage("No service filter specified");
       if (!arguments.containsKey(MONTH))
           return printUsage("Month not specified");
       if (!(arguments.containsKey(DAY) || arguments.containsKey(DAY_OF_WEEK)))
           return printUsage("Day or Day of week not specified");
       if (!arguments.containsKey(HOUR))
           return printUsage("Hour not specified");
       if (!arguments.containsKey(MINUTE))
           return printUsage("Minute not specified");
       return true;
   }

   public static boolean printUsage(String msg) {
       StringBuffer usage = new StringBuffer();
       usage.append("\nremoveReconUnit: " + msg + "\n");
       usage.append("\nusage: removeReconUnit -[argument] ? \"[value]\"\n");
       usage.append("\n");
       usage.append("-serviceprofile      Profile Name of the Service");
       usage.append(" (e.g., WinLocalProfile)\n");
       usage.append("-servicefilter       Ldap Filter to search for a Service instance\n");
       usage.append("-month               month of recon. Valid values 1-12, -1 indicates Monthly, \n");
       usage.append("                     0 to ignore\n");
       usage.append("-day                 day of recon. Valid values 1-31, -1 indicates Daily, \n");
       usage.append("                     0 to ignore\n");
       usage.append("-dayofweek           day of week. Valid values 1-7.(1 = Sunday, 2 = Monday,..),\n");
       usage.append("                     0 to ignore\n");
       usage.append("\nNOTE: Either day or dayofweek is mandatory. \n");
       usage.append("If specified both, then dayofweek will be ignored\n\n");
       usage.append("-hour                hour of recon. Valid values 0-23, -1 indicates Hourly.\n");
       usage.append("-minute              minute of recon. Valid values 0-59.\n\n");
       usage.append("\nExample: removeReconUnit -serviceprofile?WinLocalProfile  -servicefilter?");
       usage.append("\"(erservicename=WinLocal)\" -month?10 -day?26 -hour?10 -minute?10 ");
       usage.append("\n\nNOTE: If negative value -1 needs to be entered for month,day or hour, then \n");
       usage.append("change the StringTokenizer to '+' instead of '-' in function \n");
       usage.append("RemoveReconUnit.parseArgs() to identify the arguments prefixed with '+'");
       System.out.println(usage);
       return false;
   }

}
