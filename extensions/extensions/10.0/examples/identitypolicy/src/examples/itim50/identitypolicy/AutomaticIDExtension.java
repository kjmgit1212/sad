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
package examples.itim50.identitypolicy;

import java.util.ArrayList;
import java.util.List;

import com.ibm.itim.script.ContextItem;
import com.ibm.itim.script.GlobalFunction;
import com.ibm.itim.script.ScriptContextDAO;
import com.ibm.itim.script.ScriptEvaluationException;
import com.ibm.itim.script.ScriptException;
import com.ibm.itim.script.ScriptExtension;
import com.ibm.itim.script.ScriptInterface;

/**
 * This is an updated version of the AutomaticIDExtension from ITIM 4.6. In ITIM
 * 4.6 AutomaticIDExtension was implemented as a FESI extension. Since FESI no
 * longer ships with ITIM, this updated version show very similar code but
 * implemented using the new ITIM script extension framework.
 */
public class AutomaticIDExtension implements ScriptExtension {

	private List<ContextItem> allItems;

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
		return allItems;
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
		allItems = new ArrayList<ContextItem>(1);

		ContextItem func = ContextItem.createGlobalFunction("getNextCount",
				new GenerateID(dao));
		allItems.add(func);

		/*
		 * Change this to be if (true) {...} to place the AutomaticIDBean
		 * directly into the scripting environment. By doing this you can then
		 * have: var cnt = id.getNextCount(baseIdentity, serviceId); in your
		 * script, and not need the GenerateID class, or ContextItem func
		 * created above.
		 * 
		 * Since ITIM 5.0, this is the preferred method.
		 */
		if (false) {
			ContextItem bean = ContextItem.createItem("id",
					new AutomaticIDBean(dao));
			allItems.add(bean);
		}
	}

	/**
	 * The GenerateID class provides the <code>getNextCount()</code> function.
	 * The actually implementation of the function is in
	 * <code>AutomaticIDBean</code>. GenerateID checks and translates the
	 * parameters before calling <code>AutomaticIDBean</code>. An alternitive
	 * to implementing a <code>GlobalFunction</code> class would be to place
	 * the entire <code>AutomaticIDBean</code> into the scripting environment.
	 * To do this, change the if statements guard in the
	 * <code>initialize()</code> code to be if (true) { ... }. Then you can
	 * access all the public methods of AutomaticIDBean in the scripting
	 * environment.
	 * 
	 * @see AutomaticIDBean
	 * @see GlobalFunction
	 */
	public static class GenerateID implements GlobalFunction {

		private AutomaticIDBean idBean;

		public GenerateID(ScriptContextDAO dao) {
			idBean = new AutomaticIDBean(dao);
		}

		public Object call(Object[] parameters)
				throws ScriptEvaluationException {
			String baseId;
			String serviceId;

			/*
			 * Since our method requires 2 parameters, of specific types we will
			 * verify our parameters here.
			 */
			if (parameters.length != 2) {
				throw new ScriptEvaluationException(
						"Expected 2 parameters, got " + parameters.length);
			}

			if (parameters[0] instanceof String) {
				baseId = (String) parameters[0];
			} else {
				throw new ScriptEvaluationException(
						"Expecting first parameter to be a String, got a "
								+ parameters[0].getClass().getName());
			}

			if (parameters[1] instanceof String) {
				serviceId = (String) parameters[1];
			} else {
				throw new ScriptEvaluationException(
						"Expecting second parameter to be a String, got a "
								+ parameters[1].getClass().getName());
			}

			// Get the result from the implementation class AutomaticIDBean.
			int result = idBean.getNextCount(baseId, serviceId);

			// We must return an Object, so wrap the int in an Integer.
			return new Integer(result);
		}
	}

}
