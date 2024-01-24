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
import com.ibm.itim.script.ExtensionBean;
import com.ibm.itim.script.ScriptContextDAO;
import com.ibm.itim.script.ScriptException;
import com.ibm.itim.script.ScriptExtension;
import com.ibm.itim.script.ScriptInterface;

/**
 * Sample scriptint extension that defines object oriented data and functions.
 * 
 * Class provided:<br>
 * Class Name : Counter<br>
 * Data Member: value<br>
 * Method : increment
 */
public class SampleObjectExtension implements ScriptExtension {

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
        // Create the List.
        allItems = new ArrayList<ContextItem>(1);

        // Create the constructor context item.
        ContextItem bean = ContextItem.createConstructor("Counter",
                CounterBean.class);
        // Add the context item to the list.
        allItems.add(bean);
    }

    /**
     * CounterBean is a simple class that provides the model (or prototype) for
     * what the scripting item should look like. Because CounterBean is going to
     * be used as an item we can construct in scripts, it <b>must</b> have a
     * default constructor. Other constructors are also allowed, but are not
     * necessary.
     */
    public static class CounterBean implements ExtensionBean {
        /**
         * This member variable is public so that it may be accessed directly
         * from scripts. Any non-public variable will not be directly accessable
         * from scripts and you will need to provide getter and setter methods
         * (which also must be public).
         */
        public Integer value;

        /**
         * The requried default (no-argument) constructor.
         */
        public CounterBean() {
            value = new Integer(1);
        }

        /**
         * Public method that changes the value of <code>value</code>.
         * 
         * @return The updated value.
         */
        public Integer increment() {
            value = new Integer(value.intValue() + 1);
            return value;
        }
    }
}
