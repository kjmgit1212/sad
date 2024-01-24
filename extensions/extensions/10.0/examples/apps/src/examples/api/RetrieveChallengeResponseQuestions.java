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
import java.util.Collection;
import java.util.Hashtable;

import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.ForgotPasswordManager;

/**
 * Example purpose: Retrieve ChallengeResponse Questions
 */
public class RetrieveChallengeResponseQuestions {

    /**
     * Command line argument names (prefixed by "-")
     */
    private static final String USER_NAME = "username";

    private static final String[][] utilParams = new String[][] {
            { USER_NAME, "No user name specified." }
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
			PlatformContext platform = null;
        try {
            String userName = (String) arguments.get(USER_NAME);
            utils.print("------ userName:" + userName);

           platform = utils.getPlatformContext();
            
            utils.print("Creating ForgotPasswordManager");
            // ForgotPasswordManager uses the subject retrieved from platform.getSubject()
            // The user/password/realm for this subject are set in the Utils.getPlatformContext() method.
            // Please see that method for more details.
            ForgotPasswordManager forgotPasswordManager = new ForgotPasswordManager(platform);
            utils.print("Retrieving the ChallengeResponse Questions");
            Collection<String> questions = forgotPasswordManager.getSecretQuestion(userName);
            for (String question:questions) {
                utils.print("Question:  " + question);
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
        usage.append("\nretrieveChallengeResponseQuestions: " + msg + "\n");
        usage.append("usage: retrieveChallengeResponseQuestions -[argument] ? \"[value]\"\n");
        usage.append("\n");
        usage.append("-username\tName of the user to retrieve questions for\n");
        usage.append("\n");
        usage
                .append("Example: retrieveChallengeResponseQuestions -username?\"jhill\"");
        return usage.toString();
    }
}
