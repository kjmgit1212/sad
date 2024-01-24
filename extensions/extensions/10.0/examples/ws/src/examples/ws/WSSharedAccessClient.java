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
package examples.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.model.pim.WSCredential;
import com.ibm.itim.ws.model.pim.WSLeaseInfo;
import com.ibm.itim.ws.model.pim.WSSharedAccess;
import com.ibm.itim.ws.services.WSExtApplicationException;
import com.ibm.itim.ws.services.WSExtLoginServiceException;
import com.ibm.itim.ws.services.WSSharedAccessService;
/**
 * This example demonstrates how to call the IBM Security Identity Manager shared access web service, WSSharedAccessService, interfaces to get 
 * all authorized credentials and credential pools, retrieve the password for a credential, or check in a credential. 
 *
 * @since 6.0
 */
public class WSSharedAccessClient extends GenericWSClient
{
    // input stream to read user inputs and selections
    private BufferedReader in = new BufferedReader(new InputStreamReader(
            System.in));
    private WSSession session = null;
    // proxy connection to the web service WSSharedAccessService interface
    private WSSharedAccessService proxy = null;

    // method in GenericWSClient  that is not being used.
    @Override
    public boolean executeOperation(Map<String, Object> mpParams)
    {
        return false;
    }

 

	/**
	 * When running this example, you can specify two arguments (login user and
	 * password). If login user and password are passed in as arguments, the
	 * program attempts to log in the user to IBM Security Identity Manager
	 * (ISIM) with the specified password. If login user and password are not
	 * specified as arguments, the program prompts the user for login user and
	 * password, and then authenticates the user to ISIM. After the program
	 * successfully authenticates the user to ISIM, it displays a menu, that
	 * enables the user to select an action option.
	 * 
	 * @param args
	 *            the login user and password
	 */
    public static void main(String[] args)
    {                
        try {
            WSSharedAccessClient tim = new WSSharedAccessClient();
            tim.runClient(args);
        } catch (Exception e) {
            System.out.println("Error executing the example!");
        }
    }
    
    private void runClient(String[] args) {
        String userID = null;
        String passwd = null;
        
        try {
            if(args.length == 2) {  // login user and password are specified in the arguments
                userID = args[0];
                passwd = args[1];
             } else {  // login user and password are not specified, prompt user to enter the information.
                 System.out.println("");
                 System.out.println("Enter User ID: ");
                 userID = in.readLine();
                 
                 System.out.println("");
                 System.out.println("Enter Password: ");
                 passwd = in.readLine();                 
             }
            // When successfully authenticate the specified user to ISIM, display a menu
            if(login(userID, passwd)) {
                consoleMenu();   
            } else {
            	System.out.println("The example program is terminated since the user cannot log in to IBM Security Identity Manager.");
            }
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        } 
    }
        
	/**
	 * Console Menu for the Identity Manager Shared Access web service example.
	 * From the menu, a user can select the following operations: 
	 * 1. Get all shared accesses (both credentials and credential pools) that 
	 *    are authorized for the login user. 
	 * 2. Get the password for the authorized credential. If the credential 
	 *    requires checkout process, ISIM checks out the credential. 
	 * 3. Check in the checked out credential. 
	 * 4. Exit the example and log out the login user.
	 */
    private void consoleMenu() {

        // Local variable
        int choice = 0;

        do {
            // Display menu graphics
            System.out.println("");
            System.out
                    .println("=======================================================");
            System.out
                    .println(" MENU SELECTION :: Shared Access Web Services Demo                    ");
            System.out
                    .println("=======================================================");
            System.out
                    .println(" Options:                                             ");
            System.out
                    .println("      1. Get authorized shared accesses");
            System.out
                    .println("      2. Get password of the credential");
            System.out
                    .println("      3. Check in a checkout credential");
            System.out
                    .println("      0. Exit                                         ");
            System.out
                    .println("=======================================================");
            System.out.println(" Select option: ");

            try {
                choice = Integer.parseInt(in.readLine());  // getting selection from user
            } catch (Exception e) {
                System.out.println("!!! Invalid Choice !!! Please try again.");
                choice = 0;
            }

            // Switch construct
            switch (choice) {
            case 1:
                System.out.println("Option \' 1. Get authorized shared accesses \' selected");
                getSharedAccess();
                break;
            case 2:
                System.out.println("Option \' 2. Get password of the credential  \' selected");
                getCredential();
                break;                          
            case 3:
                System.out.println("Option \' 3. Check in a checkout credential \' selected");
                checkin();
                break;                  
            case 0:
                logout();
                System.out.println("Exited !");
                break;
            default:
                System.out.println("!!! Invalid Choice !!! Please try again.");
            }
            if(choice > 0)
                pressToContinue();
        } while (choice > 0 && choice < 4);
    }

    /**
     * When invalid data are entered, display the message.
     */
    private void usage(int i) {
        switch (i) {
        case 1:
            // The service URI is the value for the Universal Resource Identifier (URI) field on the service form.
            // You can log on to ISIM admin console to see the URI of the service.
            System.out.println("Please enter valid value for Service URI");            
            break;
        case 2:
            // The authorized credential or credential pool distinguised name (DN) is displayed through the option 1 selection in the menu. 
            System.out.println("Please enter valid value for Credential or Credential Pool DN");
            break;
        case 3:
            // The lease distinguised name (DN) is displayed through the option 2 selection in the menu.  
            // When the credential is checked out (using option 2 in the menu), the example displays the lease DN.
            // You must specify the lease DN of the credential that you want to check in.
            System.out.println("Please enter valid value for Lease DN");
            break;
        default:
            System.out.println("Invalid selection");
        }
    }  
    
	/**
	 * This method demonstrates how to call the WSSharedAccessService.login
	 * interface with the login user and password. The web service login
	 * interface attempts to authenticate the user to ISIM.
	 * 
	 * @param principal
	 *            user ID of the login user.
	 * @param credential
	 *            password for the login user.
	 * @return true if authentication to ISIM is success.
	 */
    private boolean login(String principal, String credential) {
        try
        {
            proxy = getSharedAccessService();
            session = proxy.login(principal, credential);
            System.out.println("Success login for user "+principal);
        }catch(WSExtLoginServiceException e)
        {
            logError(e);
        }         
        if(session != null)
            return true;
        else
            return false;
    }
    
	/**
	 * This method demonstrates how to call the WSSharedAccessService.logout
	 * interface to log out the login user. The web service logout interface
	 * logs out the login user that is stored in the session.
	 */
    private void logout() {
        if(session != null) {
            try {
                proxy.logout(session);
                System.out.println("You are successfully logged out.");
            }
            catch(WSExtApplicationException e)
            {
                logError(e);
            }
        }            
    }
    
	/**
	 * This method prompts the user to enter the service URI. Service URI is the
	 * value of erURI attribute of the service. The method finds all credentials
	 * or credential pools on the specified service that are authorized for the
	 * login user.
	 */
    private void getSharedAccess() {
        String serviceURIorDN = null; // service URI attribute
        String excludeCheckedOut = null;
        String serviceSearchOption = null;
        try {
            System.out.println("");
            System.out.println("Enter service search option:  uri (default) | dn ");
            serviceSearchOption = in.readLine();
            System.out.println("Enter Service URI or DN (value of erURI attribute of the service or DN): ");
            serviceURIorDN = in.readLine();
            System.out.println("");
            System.out.println("Do you want to exclude checked out credentials?: yes (default) | no ");
            excludeCheckedOut = in.readLine();

            boolean searchByURI = true; // default value
            if (serviceSearchOption != null && serviceSearchOption.length() > 0) {
            	serviceSearchOption = serviceSearchOption.trim().toLowerCase();
            	if (serviceSearchOption.equals("dn")) {
            		searchByURI = false;
            	}
            }
                        
            if (serviceURIorDN != null && serviceURIorDN.trim().length() > 0) {
            	Boolean excludeCheckedOutB = Boolean.TRUE;
            	if(excludeCheckedOut != null) {
            		excludeCheckedOut = excludeCheckedOut.trim().toLowerCase();
            		excludeCheckedOutB = new Boolean(!excludeCheckedOut.equals("no"));
            	}
                processGetSharedAccess(serviceURIorDN, excludeCheckedOutB, searchByURI);                
            } else {
                usage(1);  // if user didn't input the URI, display the message for invalid input
            }

        } catch (IOException e) {  // error in getting user input
            System.out.println("Error! " + e.getMessage());
        } 
    } 
    
	/**
	 * This method demonstrates how to call the
	 * WSSharedAccessService.getAuthorizedSharedAccess interface to retrieve all
	 * authorized credentials and credential pools on the specified service for
	 * the login user. The web service getAuthorizedSharedAccess interface
	 * searchs for the service that matches the specified service URI. It
	 * searchs for any service that has erURI attribute equals to the specified
	 * service URI. The service URI must be unique, and the search must find
	 * only one service that matches the specified service in ISIM. The
	 * interface finds all authorized credentials and credential pools on the
	 * service for the login user (the login user information is stored in the
	 * session).
	 * 
	 * @param serviceURIorDN
	 *            the URI or DN of the service. This value must be unique in ISIM.
	 *            There must be only one service in ISIM that matches this URI.
	 * @param excludeCheckedOut  The boolean flag to exclude the credential and
	 * credential pools that are already checked out by logged user or by other
	 * user.
	 */
    private void processGetSharedAccess(String serviceURIorDN, Boolean excludeCheckedOut, boolean searchByURI) {
        List<WSSharedAccess> returnedSharedAccess;
        try
        {
        	if (searchByURI) {
        		if (excludeCheckedOut) {
        			returnedSharedAccess=proxy.getAuthorizedSharedAccess(session, serviceURIorDN);
        		} else {
        			returnedSharedAccess=proxy.getAllAuthorizedSharedAccess(session, serviceURIorDN);
        		}
        	} else {  // search by service DN
        		if (excludeCheckedOut) {
        			returnedSharedAccess=proxy.getAuthorizedSharedAccessByServiceDN(session, serviceURIorDN);
        		} else {
        			returnedSharedAccess=proxy.getAllAuthorizedSharedAccessByServiceDN(session, serviceURIorDN);
        		}
        	}
            Iterator <WSSharedAccess> it = returnedSharedAccess.iterator();
            StringBuffer sharedAccessBuff = new StringBuffer();
            while (it.hasNext()) {
                WSSharedAccess temp = (WSSharedAccess)it.next();
                // display the credential or credential pool DN
                sharedAccessBuff.append("CredCompDn =" + temp.getCredCompDN());
                sharedAccessBuff.append("\t | ");
                // display the name of credential or credential pool
                sharedAccessBuff.append("name ="+ temp.getName());
                sharedAccessBuff.append("\t | ");
                // display the description credential or credential pool 
                sharedAccessBuff.append("description =" + temp.getDescription());
                sharedAccessBuff.append("\n");
            
            }
            System.out.println("Please copy the DN of the credential or credential pool that you want to get the password.\n");
            System.out.println(sharedAccessBuff);            
        }
        catch(WSExtApplicationException e)
        {
            logError(e);
        }
        catch(WSExtLoginServiceException e)
        {
            logError(e);
        }        
    }
    
	/**
	 * This method prompts user to enter the DN of credential or credential pool
	 * (required), a short justification (optional), and a checkout duration
	 * (optional). For more information on justification and checkout duration,
	 * please see Identity Manager Administration Guide section on Shared
	 * Access.
	 */
    private void getCredential() {
        String credCompDn = null;
        String justification = "";
        String duration = "";
        boolean invalidDuration = false;
        BigInteger intDuration = null;
        try {
            System.out.println("");
            System.out.println("Enter the DN of the credential or credential Pool (value that you copied in \'Get authorized shared accesses\'): ");
            credCompDn = in.readLine();
            
            System.out.println("");
            System.out.println("Enter Justification: ");
            justification = in.readLine();

            System.out.println("");
            System.out.println("Enter checkout duration (in hour): ");
            duration = in.readLine();
            
            if(duration != null && duration.trim().length() > 0) {
                try {
                    intDuration = new BigInteger(duration);
                } catch (NumberFormatException nfe) {
                    invalidDuration = true;
                }
            }
                        
            if (credCompDn == null || credCompDn.trim().length() == 0) {
                usage(2);               
            } else if(invalidDuration) {
                System.out.println("Invalid checkout duration.  Please enter an integer value for checkout duration");
            } else {
                processGetCredential(credCompDn, justification,intDuration); // success getting user inputs
            }

        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        } 
    } 
    
	/**
	 * This method demonstrates how to call the
	 * WSSharedAccessService.getCredential interface to retrieve credential
	 * information for the authorized credential or credential pool. It also
	 * shows how to retrieve the lease information from the WSCredential. The
	 * web service getCredential interface retrieves the credential information
	 * of specified credential or credential pool DN. If the credential requires
	 * the checkout process and it is not checked out, the system checks out the
	 * credential. When the credential is checked out, the lease is created and
	 * its information is returned in WSCredential. If the credential does not
	 * require checkout, only the password is returned in WSCredential. The
	 * lease information is not returned in WSCredential.
	 * 
	 * @param credCompDn
	 *            - the Distinguished name (DN) of the credential or credential
	 *            pool. This DN is displayed through option 1 in the menu.
	 * @param text
	 *            - a short justification for the checkout process
	 * @param duration
	 *            - a checkout duration for the checkout process.
	 */
    private void processGetCredential(String credCompDn, String text, BigInteger duration) {
        try
        {
            WSCredential wsCredential = proxy.getCredential(session, credCompDn, text, duration);
            if (wsCredential != null) {
                String userID = wsCredential.getUserID();
            	String paswd = wsCredential.getPassword();
                System.out.println("User ID : "+ userID);
                System.out.println("Password: "+paswd);  // print out the password of the credential
                WSLeaseInfo leaseInfo = wsCredential.getLeaseInfo();
                if(leaseInfo != null) {
                	XMLGregorianCalendar calendar = leaseInfo.getExpiration();
                	if(calendar != null)
                		System.out.println("Lease Expiration: "+calendar.toString());
                    System.out.println("Please copy the following lease DN of this credential.   It will be used for checkin.\n");
                    System.out.println("Lease DN: "+leaseInfo.getLeaseDN());
                }
                Boolean isPasswordViewable = wsCredential.isIsPasswordViewable();
                if(isPasswordViewable != null) {
                	System.out.println("Is password viwable: " + isPasswordViewable.toString());
                }
            }            
        }
        catch(WSExtApplicationException e)
        {
            logError(e);
        }
        catch(WSExtLoginServiceException e)
        {
            logError(e);
        }              
    }

	/**
	 * This method prompts user to enter the lease DN of the checked out
	 * credential (required). The method checks in the specified lease.
	 */   
    private void checkin() {
        String leaseDn = null;
        try {
            System.out.println("");
            System.out.println("Enter the lease DN of the credential that you want to check in: ");
            leaseDn = in.readLine();            
                        
            if (leaseDn == null || leaseDn.trim().length() == 0) {
                usage(3);               
            } else {
                processCheckin(leaseDn);
            }

        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        }        
    }
    
	/**
	 * This method demonstrates how to call the WSSharedAccessService.checkIn
	 * interface to check in the checked out credential. The web service checkIn
	 * interface checks in the checked out credential. When the credential is
	 * successfully checked in, the request ID is returned.
	 * 
	 * @param leaseDn
	 *            - the Distinguished name (DN) of the lease of the credential.
	 *            This DN is displayed through the option 2 in the menu.
	 */
    private void processCheckin(String leaseDn) {
        try
        {
            String requestID = proxy.checkIn(session, leaseDn);            
            if (requestID != null) {
                System.out.println("Request ID: "+requestID);
            }       
            System.out.println("The credential is successfully checked in.");
        }
        catch(WSExtApplicationException e)
        {
            logError(e);
        }
        catch(WSExtLoginServiceException e)
        {
            logError(e);
        }         
    }
    
    private void pressToContinue() {
        try {
            System.out.println("Press ENTER key to continue !!!");
            in.readLine();
        } catch(IOException ex) {            
        }            
    }    
    
	/**
	 * This method demonstrates how to extract the Identity Manager message ID
	 * and parameters from the web service WSExtApplicationException fault. For
	 * details on message IDs, see the Identity Manager Error Messages
	 * Reference.
	 * 
	 * @param ex
	 *            the WSExtApplicationException
	 */
    private void logError(WSExtApplicationException ex) {
        com.ibm.itim.ws.exceptions.pim.WSExtApplicationException wsEx = ex.getFaultInfo();
        System.out.println("Message: " + ex.getMessage());
        System.out.println("Message ID: " + wsEx.getMsgKey());
        System.out.println("Message Parameters: " + wsEx.getMsgParameters());
        String cause = ex.getMessage();
        Throwable t = ex.getCause();
        if (t != null) {
        	cause = t.getMessage();
        }
        System.out.println("Root Cause: "+cause);
    }
    
	/**
	 * This method demonstrates how to extract the Identity Manager message ID
	 * and parameters from the web service WSExtLoginServiceException fault. For
	 * details on message IDs, see the Identity Manager Error Messages
	 * Reference.
	 * 
	 * @param ex
	 *            the WSExtLoginServiceException
	 */
    private void logError(WSExtLoginServiceException ex) {
        com.ibm.itim.ws.exceptions.pim.WSExtLoginServiceException wsEx = ex.getFaultInfo();
        System.out.println("Message: " + ex.getMessage());
        System.out.println("Message ID: " + wsEx.getMsgKey());
        System.out.println("Message Parameters: " + wsEx.getMsgParameters());
        String cause = ex.getMessage();
        Throwable t = ex.getCause();
        if (t != null) {
        	cause = t.getMessage();
        }
        System.out.println("Root Cause: "+cause);
    }    
    
}
