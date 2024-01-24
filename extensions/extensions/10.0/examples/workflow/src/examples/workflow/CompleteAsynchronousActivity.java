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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.ibm.itim.logging.JLogUtil;
import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.workflow.model.ActivityEntity;
import com.ibm.itim.workflow.model.WorkflowException;
import com.ibm.log.Level;

public class CompleteAsynchronousActivity implements Runnable {

	private Timer timer;

	// run method to run CompleteAsynchronousActivity as a daemon thread
	public void run() {
		timer = new Timer(true);
		timer.schedule(new CompleteActivity(), 0, 60 * 1000);
	}

	class CompleteActivity extends TimerTask {
		public void run() {
			complete();
		}
	}

	private void complete() {
		File file = new File(
				AsynchronousApplicationExtension.EXAMPLE_ACTIVITY_FILE);

		if (file.exists()) {
			long activityId = 0;

			BufferedReader in;
			try {
				in = new BufferedReader(new FileReader(file));

				String strActivityId = in.readLine();
				in.close();
				activityId = (new Long(strActivityId)).longValue();
			} catch (FileNotFoundException e1) {
				; // Do nothing
			} catch (IOException e) {
				; // Do nothing
			}

			file.delete();

			if (activityId > 0) {
				try {
					ActivityEntity act = ActivityEntity.getActivity(activityId);
					act.complete();
					SystemLog.getInstance().logInformation(this,
							"Completed activity: " + activityId);
				} catch (WorkflowException ex) {
					JLogUtil.getTraceLogger(CompleteAsynchronousActivity.class)
							.exception(Level.DEBUG_MIN, this, "complete()", ex);
					SystemLog.getInstance().logError(this,
							"Not able to complete activity" + activityId);
				}
			}
		}
	}

}
