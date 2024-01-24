/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2005, 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/

package examples.api;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.ForgotPasswordManager;
import com.ibm.itim.apps.identity.ForgotPasswordRequest;

/**
 * Example purpose: Retrieve ChallengeResponse Questions
 */
public class ChangeOrResetPasswordUsingChallengeResponse {

    /**
     * Command line argument names (prefixed by "-")
     */
    private static final String USER_NAME = "username";
    
    private static final String CHANGEPASSWORD = "changepassword";
    
    private static final String PASSWORD = "password";
    
    private static final String QUESTION = "question";

    private static final String[][] utilParams = new String[][] {
            { USER_NAME, "No user name specified." },
            { CHANGEPASSWORD, "Change Password not specified" },
            { PASSWORD, "No password specified" },
            { QUESTION, "No question specified" }
    };

    public static void main(String[] args) {
        run(args, true);
    }

    /**
     * Run the example.
     * 
     * @param args
     *            Arguments passed in, usually from the command line. See usage
     *            from more information.
     * @param verbose
     *            Should the program print out lots of information.
     * @return true if run() completes successfully, false otherwise.
     */
    public static boolean run(String[] args, boolean verbose) {
        Utils utils = null;
        Hashtable<String, Object> arguments = null;
        try {
            utils = new Utils(utilParams, verbose);
            arguments = utils.parseArgs(args);
        } catch (IllegalArgumentException ex) {
            if (verbose) {
                System.err.println(getUsage(ex.getMessage()));
            }
            return false;
        }
			PlatformContext platform=null;
        try {
            String userName = (String) arguments.get(USER_NAME);
            utils.print("------ userName:" + userName);
            
            String password = (String) arguments.get(PASSWORD);
            utils.print("------ password:" + password);
            
            boolean changePassword = Boolean.parseBoolean((String)arguments.get(CHANGEPASSWORD));
            utils.print("------ changePassword:" + changePassword);

            platform = utils.getPlatformContext();
            
            utils.print("Creating ForgotPasswordManager");
            // ForgotPasswordManager uses the subject retrieved from platform.getSubject()
            // The user/password/realm for this subject are set in the Utils.getPlatformContext() method.
            // Please see that method for more details.
            ForgotPasswordManager forgotPasswordManager = new ForgotPasswordManager(platform);
            Map<String, String> candr = new HashMap<String, String>();

            // set attributes specified on the command-line
            Object attrs = arguments.get(QUESTION);
            if (attrs instanceof Vector) {
                Vector attributes = (Vector) attrs;
                Iterator it = attributes.iterator();
                while (it.hasNext()) {
                    String questionAndAnswer = (String) it.next();
                    String[] quesAnswer = questionAndAnswer.split("=");
                    utils.print("Adding question:  " + quesAnswer[0] + " with answer:  " + quesAnswer[1]);                    
                    candr.put(quesAnswer[0], quesAnswer[1]);
                }
            } else if (attrs instanceof String) {
                String questionAndAnswer = (String) attrs;
                String[] quesAnswer = questionAndAnswer.split("=");
                utils.print("Adding question:  " + quesAnswer[0] + " with answer:  " + quesAnswer[1]);                    
                candr.put(quesAnswer[0], quesAnswer[1]);
            }
            
            ForgotPasswordRequest fpr = null;
            if (changePassword) {
                utils.print("Changing password using ChallengeResponse");
                fpr = forgotPasswordManager.changePassword(userName, candr, password);
            } else {
                utils.print("Resetting password using ChallengeResponse");
                fpr = forgotPasswordManager.resetPassword(userName, candr);
            }
            if (fpr != null) {
                System.out.println("Request ID:  " + fpr.getRequestID());
            }
            
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        } catch (SchemaViolationException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
			if(platform != null) {
				platform.close();
				platform = null;
			}
		}
        return true;
    }

    public static String getUsage(String msg) {
        StringBuffer usage = new StringBuffer();
        usage.append("\nchangeOrResetPasswordUsingChallengeResponse: " + msg + "\n");
        usage.append("usage: changeOrResetPasswordUsingChallengeResponse -[argument] ? \"[value]\"\n");
        usage.append("\n");
        usage.append("-username\tName of the user whose password is to be modified\n");
        usage.append("-password\tNew password for the user (ignored if changepassword is false)\n");
        usage.append("-changepassword\tIf true then change password else reset password\n");
        usage.append("-question\tQuestion and answer for challenge response authentication\n");
        usage.append("\n");
        usage
                .append("Example: changeOrResetPasswordUsingChallengeResponse ");
        usage.append("-username?\"jhill\" "); 
        usage.append("-password?\"secretpass\" ");
        usage.append("-changepassword?\"true\" ");
        usage.append("-question?\"How do you spell secret?=secret\" ");
        return usage.toString();
    }
}
