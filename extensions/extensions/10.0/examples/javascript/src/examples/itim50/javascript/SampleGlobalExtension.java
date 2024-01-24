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
package examples.itim50.javascript;

import java.util.ArrayList;
import java.util.List;

import com.ibm.itim.script.ContextItem;
import com.ibm.itim.script.GlobalFunction;
import com.ibm.itim.script.ScriptContextDAO;
import com.ibm.itim.script.ScriptEvaluationException;
import com.ibm.itim.script.ScriptException;
import com.ibm.itim.script.ScriptExtension;
import com.ibm.itim.script.ScriptInterface;

public class SampleGlobalExtension implements ScriptExtension {

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

        // Create the List to return later.
        allItems = new ArrayList<ContextItem>(2);

        /*
         * Create the ContextItems we need; one for counter and one for
         * increment and add each item to the allItems list.
         */
        ContextItem counter = ContextItem.createItem("counter", new Integer(1));
        allItems.add(counter);

        ContextItem func = ContextItem.createGlobalFunction("increment",
                new UpdateFunction(dao));
        allItems.add(func);
    }

    /**
     * To add a global function to the scripting environment you must have a
     * class that implements <code>GlobalFunction</code>.
     */
    public class UpdateFunction implements GlobalFunction {

        /**
         * dao will give us access to the scripting environment.
         */
        private ScriptContextDAO dao;

        public UpdateFunction(ScriptContextDAO context) {
            dao = context;
        }

        /**
         * This is the method that is called when a script calls the new global
         * function that you added. It is your responsibility to check that the
         * parameters are all there and they are the correct types.
         */
        public Object call(Object[] parameters)
                throws ScriptEvaluationException {

            // Get the counter item from the scripting environment.
            Integer counter = (Integer) dao.lookupItem("counter");

            // Update the value.
            counter = new Integer(counter.intValue() + 1);

            // Put the new value back into the scripting environment.
            dao.updateContextItem("counter", counter);

            /*
             * Return the value. This can also return null if your function does
             * not return a value.
             */
            return counter;
        }
    }
}
