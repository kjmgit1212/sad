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

import com.ibm.itim.workflow.application.WorkflowApplication;
import com.ibm.itim.workflow.application.WorkflowExecutionContext;
import com.ibm.itim.workflow.model.ActivityResult;

/**
 * Custom class for synchronous activity
 */
public class SynchronousApplicationExtension implements WorkflowApplication {

	protected WorkflowExecutionContext ctx;

	public SynchronousApplicationExtension() {
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
	 * Perform change password extension synchronously
	 * 
	 * @return ActivityResult The result of the activity. If summary==PENDING,
	 *         then the activity will be executed asynchronously; otherwise the
	 *         activity is completed. There is no detail returned.
	 * 
	 */
	public ActivityResult synchronousChangePasswordExtension() {

		System.out
				.println(this
						+ "--> Entering example for synchronous change password extension acitivity");
		try {
			String activityId = String.valueOf(ctx.getActivityVO().getId());

			return new ActivityResult(ActivityResult.STATUS_COMPLETE,
					ActivityResult.SUCCESS,
					"Scheduled for synchronous change password extension activity "
							+ activityId, null);
		} catch (Exception e) {
			return new ActivityResult(ActivityResult.FAILED, e.getClass()
					.getName()
					+ ": " + e.getMessage(), null);
		}
	}

}
