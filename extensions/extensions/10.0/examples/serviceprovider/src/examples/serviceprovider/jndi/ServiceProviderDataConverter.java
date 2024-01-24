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

package examples.serviceprovider.jndi;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;

import com.ibm.itim.common.AttributeChangeIterator;
import com.ibm.itim.common.AttributeChangeOperation;
import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValueIterator;
import com.ibm.itim.common.AttributeValues;

public class ServiceProviderDataConverter {
	// need to handle enRole password attribute expectation
	private static final String ENROLE_PASSWORD_ATTRIBUTE = "erpassword";

	private static final String PASSWORD_ATTRIBUTE = "userpassword";

	public ServiceProviderDataConverter() {
	}

	public Attribute createJNDIAttribute(AttributeValue av) {
		String name = av.getName();

		// convert enrole attribute names to jndi example name
		if (name.equalsIgnoreCase(ENROLE_PASSWORD_ATTRIBUTE))
			name = PASSWORD_ATTRIBUTE;

		BasicAttribute attr = new BasicAttribute(name);

		Collection rawValues = av.getValues();
		Iterator rawIter = rawValues.iterator();

		// could be multi-valued
		while (rawIter.hasNext())
			attr.add(rawIter.next());

		return attr;
	}

	public Attributes createJNDIAttributes(AttributeValues avs) {
		AttributeValueIterator iter = avs.iterator();

		BasicAttributes attributes = new BasicAttributes();

		while (iter.hasNext())
			attributes.put(this.createJNDIAttribute((AttributeValue) iter
					.next()));

		return attributes;
	}

	public ModificationItem[] createJNDIModifications(
			AttributeChangeOperation op) {
		return (ModificationItem[]) this.createJNDIModificationCollection(op)
				.toArray();
	}

	private Collection<ModificationItem> createJNDIModificationCollection(
			AttributeChangeOperation op) {
		int action = op.getModificationAction();

		Collection<AttributeValue> attributes = op.getChangeData();

		Vector<ModificationItem> mods = new Vector<ModificationItem>(attributes
				.size());

		for (AttributeValue av : attributes) {
			Attribute attr = createJNDIAttribute(av);
			mods.add(new ModificationItem(action, attr));
		}

		return mods;
	}

	public ModificationItem[] createJNDIModifications(AttributeChanges ops) {
		AttributeChangeIterator iter = ops.iterator();

		Vector<ModificationItem> completeMods = new Vector<ModificationItem>();

		while (iter.hasNext()) {
			completeMods.addAll(createJNDIModificationCollection(iter.next()));
		}

		ModificationItem[] modItems = new ModificationItem[completeMods.size()];

		Iterator modsIter = completeMods.iterator();
		int i = 0;
		while (modsIter.hasNext()) {
			modItems[i++] = (ModificationItem) modsIter.next();
		}

		return modItems;
	}

	public AttributeValue createModelAttribute(Attribute attribute) {
		AttributeValue av = null;

		String name = attribute.getID();

		Vector<Object> values = new Vector<Object>();

		try {
			NamingEnumeration jndiValues = attribute.getAll();

			while (jndiValues.hasMore())
				values.add(jndiValues.next());

			av = new AttributeValue(name, values);
		} catch (NamingException ne) {
		}

		return av;
	}

	public AttributeValues createModelAttributes(Attributes attributes) {
		AttributeValues values = new AttributeValues();

		try {
			NamingEnumeration _enum = attributes.getAll();

			while (_enum.hasMore()) {
				Attribute attr = (Attribute) _enum.next();

				// don't return passwords
				if (!attr.getID().equalsIgnoreCase(PASSWORD_ATTRIBUTE)) {
					AttributeValue value = this.createModelAttribute(attr);

					if (value != null)
						values.put(value);
				}
			}
		} catch (NamingException ne) {
		}

		return values;
	}
}
