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
package examples.workflow.customApproval.itim50;

import java.util.ArrayList;
import java.util.List;

import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.script.ContextItem;
import com.ibm.itim.script.GlobalFunction;
import com.ibm.itim.script.ScriptContextDAO;
import com.ibm.itim.script.ScriptEvaluationException;
import com.ibm.itim.script.ScriptException;
import com.ibm.itim.script.ScriptExtension;
import com.ibm.itim.script.ScriptInterface;

public class CustomApprovalExtension implements ScriptExtension {

	List<ContextItem> allBeans;

	/**
	 * <code>getContextItems()</code> is called before any items are loaded
	 * into the scripting environment. In the simplist case, just return the
	 * List created during <code>initialize()</code>. It is also common to
	 * save references to any <code>ContextItem</code>s you create in
	 * <code>initialize()</code> as well as a reference to the
	 * <code>ScriptInterface</code> object passed into
	 * <code>initialize()</code>. With the <code>ScriptInterface</code> and
	 * <code>ContextItem</code>s you can update the state of each
	 * <code>ContextItem</code> right before returning the <code>List</code>.
	 * 
	 * @return A List of <code>ContextItem</code>s.
	 */
	public List getContextItems() {
		return allBeans;
	}

	/**
	 * Initialize the extension.
	 * 
	 * @param si
	 *            The ScriptInterface passed by the host component. If your
	 *            extension needs information from the host component it will be
	 *            passed in through the ScriptInterface. You will need to
	 *            downcast the ScriptInterface to a specific type before using
	 *            it. Check the documentation for which specific types each host
	 *            component provides.
	 * @param dao
	 *            The ScriptContextDAO is an object that contains all of the
	 *            state of the scripting environment. Save this item if you need
	 *            to access the scripting state, or you need to modifiy the
	 *            state later.
	 * 
	 * @throws ScriptException
	 *             Throw a ScriptException if something unexpected goes wrong
	 *             while you are initializing the extension.
	 * @throws IllegalArgumentException
	 *             Throw an IllegalArgumentException if you need si to be
	 *             downcastable to a specific type and it is not. This indicates
	 *             that the extension is being loaded from an unsupported host
	 *             component.
	 */
	public void initialize(ScriptInterface si, ScriptContextDAO dao)
			throws ScriptException, IllegalArgumentException {

		allBeans = new ArrayList<ContextItem>();

		FindApproverFunction function = new FindApproverFunction();
		ContextItem findApproverFunc = ContextItem.createGlobalFunction(
				"findApprover", function);

		allBeans.add(findApproverFunc);
	}

	/**
	 * To add a global function to the scripting environment you must have a
	 * class that implements <code>GlobalFunction</code>.
	 */
	public class FindApproverFunction implements GlobalFunction {

		CustomApprovalBean bean;

		public FindApproverFunction() {
			bean = new CustomApprovalBean();
		}

		/**
		 * This is the method that is called when a script calls the new global
		 * function that you added. It is your responsibility to check that the
		 * parameters are all there and they are the correct types.
		 */
		public Object call(Object[] parameters)
				throws ScriptEvaluationException {

			String groupName = parameters[0] == null ? null : parameters[0]
					.toString();

			SystemLog logger = SystemLog.getInstance();
			logger.logInformation(this, "call(" + groupName + ")");

			String approverDN = bean.findApprover(groupName);

			logger.logInformation(this, "call() returned: " + approverDN);

			/*
			 * Return the value. This can also return null if your function does
			 * not return a value.
			 */
			return approverDN;
		}
	}

}
