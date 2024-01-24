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

package examples.workflow;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.ibm.itim.logging.SystemLog;

/**
 * This class is used to startup the complete asynchronous activity. The servlet
 * is set to load on startup.
 */
public class ExampleStartupServlet extends GenericServlet {
	private static final long serialVersionUID = -1202746859697962770L;

	private static final SystemLog logger = SystemLog.getInstance();

	static Thread _completeAsynchronousActivity = null;

	/**
	 * Used to lock creation of the CompleteAsynchronousActivity thread
	 */
	public final static String COMPLETE_ASYNCH_ACTIVITY_LOCK = "COMPLETE_ASYNCH_ACTIVITY_LOCK";

	/**
	 * 
	 */
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {

	}

	/**
	 * This method is called when the servlet is loaded. It creates the <CODE>CompleteAsynchronousActivity</CODE>
	 * thread and starts it. The synchronization in the method is used to
	 * prevent multiple CompleteAsynchronousActivity threads from being created.
	 * This is necessary as there is no gaurentee in the J2EE spec about home
	 * many instances of this servlet will be created.
	 * 
	 * @exception ServletException
	 * @see javax.servlet.GenericServlet
	 */
	public void init() throws ServletException {
		super.init();

		logger.logInformation(this, "ExampleStartupServlet init called");

		synchronized (COMPLETE_ASYNCH_ACTIVITY_LOCK) {
			logger.logInformation(this,
					"Acquired Complete Asynchronous Activity Lock");

			if (_completeAsynchronousActivity == null) {
				/*
				 * if Complete Asynchronous Activity thread has not already been
				 * created
				 */
				logger.logInformation(this,
						"Creating Complete Asynchronous Activity Thread");

				CompleteAsynchronousActivity completeAsynchronousActivity = new CompleteAsynchronousActivity();
				_completeAsynchronousActivity = new Thread(
						completeAsynchronousActivity);
				_completeAsynchronousActivity.setDaemon(true);
				_completeAsynchronousActivity.start();

				logger.logInformation(this,
						"Started Complete Asynchronous Activity Thread");
			} else {
				logger.logWarning(this, "Complete Asynchronous Activity "
						+ "Thread already exists skipping creation");
			}
		}
	}

}
