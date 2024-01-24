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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.workflow.application.WorkflowApplication;
import com.ibm.itim.workflow.application.WorkflowExecutionContext;
import com.ibm.itim.workflow.model.ActivityResult;

/**
 * Custom class for asynchronous activity
 */
public class AsynchronousApplicationExtension implements WorkflowApplication {

	public static final String EXAMPLE_ACTIVITY_FILE = "ExampleActivity.id";

	protected WorkflowExecutionContext ctx;

	public AsynchronousApplicationExtension() {
	}

	/**
	 * Passes the workflow execution context to the application.
	 * 
	 * @param context
	 *            WorklowExecutionContext holding information about the
	 *            currently executing activity.
	 */
	public void setContext(WorkflowExecutionContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * Perform change password extension asynchronously
	 * 
	 * @return ActivityResult The result of the activity. If summary==PENDING,
	 *         then the activity will be executed asynchronously; otherwise the
	 *         activity is completed. There is no detail returned.
	 * 
	 */
	public ActivityResult asynchronousChangePasswordExtension() {

		SystemLog logger = SystemLog.getInstance();
		logger.logInformation(this, "Entering example for asynchronous change "
				+ "password extension acitivity");

		try {
			String activityId = String.valueOf(ctx.getActivityVO().getId());
			BufferedWriter out = new BufferedWriter(new FileWriter(
					EXAMPLE_ACTIVITY_FILE));
			out.write(activityId);
			out.close();

			String msg = "Scheduled for asynchronous change password extension activity:  "
					+ activityId;
			return new ActivityResult(ActivityResult.STATUS_WAIT,
					ActivityResult.PENDING, msg, null);
		} catch (IOException ex) {
			String message = ex.getClass().getName() + ": " + ex.getMessage();
			return new ActivityResult(ActivityResult.FAILED, message, null);
		}
	}

}
