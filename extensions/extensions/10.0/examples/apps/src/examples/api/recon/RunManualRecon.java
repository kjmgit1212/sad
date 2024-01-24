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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.recon.ReconManager;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;

import examples.api.Utils;

/**
 * Sample command-line Java class to run recon for a manual service or connected manual service.  
 *
 * To run a manual service recon -
 *  Specify the required arguments for running the 'runManualServiceRecon' of the class-
 *    com.ibm.itim.apps.recon.ReconManager.
**/
public class RunManualRecon { 

   /**
    * Command line argument names (prefixed by "-")
    */
   private static final String SERVICE_PROFILE          = "serviceprofile";
   private static final String SERVICE_FILTER           = "servicefilter";
   private static final String SUPPORTING_DATA_ONLY     = "supportingdataonly";
   private static final String CSV_PATH                 = "csvpath";
   
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
           
           String csvPath = (String)arguments.get(CSV_PATH);
           String fileData = getCSVData(csvPath);
           if (fileData == null || fileData.length() == 0) {
               System.out.println("Unable to load CSV data.\n");
               System.exit(0);
           }

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

           
           String supportingDataOnlyString = (String)arguments.get(SUPPORTING_DATA_ONLY);
           boolean supportingDataOnly = false;
           if(supportingDataOnlyString != null) {
               supportingDataOnly = (Boolean.valueOf(supportingDataOnlyString).booleanValue());
           }

           System.out.println("Submitting request to run recon");
           Request request = reconManager.runManualServiceRecon(serviceMO.getDistinguishedName(), 
                                supportingDataOnly, fileData);
           System.out.println("Run recon submitted successfully\n");
           System.out.println("Process ID = " + request.getID());
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
       } catch (Exception e) {
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
           return printUsage("No servicefilter specified");
       return true;
   }

   public static boolean printUsage(String msg) {
       StringBuffer usage = new StringBuffer();
       usage.append("\nrunManualRecon: " + msg + "\n");
       usage.append("usage: runManualRecon -[argument] ? \"[value]\"\n");
       usage.append("\n");
       usage.append("-serviceprofile      Profile Name of the Service");
       usage.append(" (e.g., WinLocalProfile)\n");
       usage.append("-servicefilter       Ldap Filter to search for a Service instance\n");
       usage.append("-supportingdataonly  Flag specifying whether this is a supporting data only reconciliation\n");
       usage.append("-csvpath             Absolute path to the csv file containing the reconciliation data\n");
       usage.append("Example: runManualRecon -serviceprofile?WinLocalProfile -servicefilter?");
       usage.append("\"(erservicename=WinLocal)\"");
       usage.append(" -csvpath?\"C:\\manualrecon.csv\"");
       System.err.println(usage);
       return false;
   }
   
   public static String getCSVData(String pathToFile) {
       StringBuffer stringBuffer = new StringBuffer();
       if (pathToFile == null || pathToFile.length() == 0) {
           return stringBuffer.toString();
       }
       
       try {
           BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathToFile))));
           String line = null;
           while ((line = br.readLine()) != null) {
               stringBuffer.append(line + "\n");
           }
       }
       catch(FileNotFoundException e) {
           // do nothing -- the empty string will be returned
       }
       catch(IOException e) {
           // do nothing -- the empty string will be returned
       }
       return stringBuffer.toString();
   }

}
