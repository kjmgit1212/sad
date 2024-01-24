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

package examples.serviceprovider.dsml2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Example for the the DSMLv2 JNDI provider interface to TIM.
 */
public class DSML2DirContextTest {

	/**
	 * Name of a key in test_data.properties file for user id attribute value
	 */
	private static final String UID = "uid";

	private static final String EQUALS = "=";

	/**
	 * Name of a key in test_data.properties file for surname attribute value
	 */
	private static final String SURNAME = "sn";

	/**
	 * Name of a key in test_data.properties file for common name attribute
	 * value
	 */
	private static final String CN = "cn";

	/**
	 * Key in test_data.properties file for distinguished name of an entity to
	 * lookup, modify, and delete
	 */
	private static final String NAME = "name";

	/**
	 * Object class to create entry with
	 */
	private static final String OBJECTCLASS = "objectclass";

	/**
	 * Key in data file for a modification item
	 */
	private static final String MODIFY = "modify";

	/**
	 * Key in data file for naming context
	 */
	private static final String NAMING_CONTEXT = "namingContext";

	/**
	 * name of the connection properties file
	 */
	private static final String PROPS = "data/test.properties";

	/**
	 * Name of the test data file
	 */
	private static final String DATA_PROPS = "data/test_data.properties";

	/**
	 * value of uid attribute read from properties file
	 */
	private String uid;

	/**
	 * value of sn attribute read from properties file
	 */
	private String surname;

	/**
	 * value of distinguished name read from properties file
	 */
	private String name;

	/**
	 * value of cn attribute read from properties file
	 */
	private String cn;

	/**
	 * value of object class read from properties file
	 */
	private String objectclass;

	/**
	 * value of modification item read from properties file
	 */
	private String modifyProp;

	/**
	 * value of naming context read from properties file
	 */
	private String namingContext;

	/**
	 * Sets up the test environment. Reads test data from the DATA_PROPS file.
	 * 
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void setUp() throws IOException {
		System.out.println();
		System.out.println("Setting up test.");
		Properties props = new Properties();
		props.load(new FileInputStream(DATA_PROPS));
		name = props.getProperty(NAME);
		uid = props.getProperty(UID);
		surname = props.getProperty(SURNAME);
		cn = props.getProperty(CN);
		objectclass = props.getProperty(OBJECTCLASS);
		modifyProp = props.getProperty(MODIFY);
		namingContext = props.getProperty(NAMING_CONTEXT);
	}

	/**
	 * Tests createSubcontext method. Adds a person to TIM with the name and
	 * attributes read from the properties file.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void testCreateSubcontext() throws NamingException, IOException {
		System.out.println();
		System.out.println("Testing creation of person.");
		DirContext context = null;
		try {
			context = getDirContext();
			Attributes attributes = new BasicAttributes();
			Attribute userIDAttr = new BasicAttribute(UID, uid);
			attributes.put(userIDAttr);
			if (surname != null) {
				Attribute surnameIDAttr = new BasicAttribute(SURNAME, surname);
				attributes.put(surnameIDAttr);
			}
			Attribute cnAttr = new BasicAttribute(CN, cn);
			attributes.put(cnAttr);
			Attribute objectclassAttr = new BasicAttribute(OBJECTCLASS,
					objectclass);
			attributes.put(objectclassAttr);
			String dn = name + "," + namingContext;
			context.createSubcontext(dn, attributes);
		} finally {
			try {
				if (context != null) {
					context.close();
				}
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Tests destroySubcontext method. Delete a person to TIM with the name read
	 * from the properties file.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void testDestroySubcontext() throws NamingException, IOException {
		System.out.println();
		System.out.println("Testing deletion of person.");
		DirContext context = null;
		try {
			String dn = name + "," + namingContext;
			context = getDirContext();
			context.destroySubcontext(dn);
		} finally {
			try {
				if (context != null) {
					context.close();
				}
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Tests lookup method. Looks up a person to TIM with the name read from the
	 * properties file.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void testLookup() throws NamingException, IOException {

		System.out.println();
		System.out.println("Testing lookup of " + name);

		DirContext context = null;
		try {
			context = getDirContext();
			String dn = name + "," + namingContext;
			Attributes attributes = ((DirContext) context.lookup(dn))
					.getAttributes("");
			assertTrue("attributes == null", attributes != null);
			NamingEnumeration _enum = attributes.getAll();
			while (_enum.hasMore()) {
				Attribute attribute = (Attribute) _enum.next();
				System.out.println("id: " + attribute.getID() + ", values: ");
				NamingEnumeration values = attribute.getAll();
				while (values.hasMore()) {
					System.out.println("\t" + values.next());
				}
			}
		} finally {
			try {
				if (context != null) {
					context.close();
				}
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Tests modifying a person. Looks up a person to TIM with the name and
	 * modification item read from the properties file.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void testModifyPerson() throws NamingException, IOException {
		System.out.println();
		System.out.println("Testing modification of person data");
		DirContext context = null;
		try {
			context = getDirContext();
			StringTokenizer st = new StringTokenizer(modifyProp, EQUALS);
			String attrName = st.nextToken();
			String value = st.nextToken();
			Attribute attribute = new BasicAttribute(attrName, value);
			ModificationItem[] modificationItem = new ModificationItem[1];
			modificationItem[0] = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, attribute);
			String dn = name + "," + namingContext;
			context.modifyAttributes(dn, modificationItem);
		} finally {
			try {
				if (context != null) {
					context.close();
				}
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Tests the root DSE search functionality. This gives information about the
	 * server.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public void testSearchRootDSE() throws NamingException, IOException {

		System.out.println();
		System.out.println("Testing root DSE search");

		DirContext context = null;
		try {

			context = getDirContext();

			String filter = "(objectClass=*)";
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
			searchControls.setDerefLinkFlag(false);
			NamingEnumeration _enum = context
					.search("", filter, searchControls);
			assertTrue("_enum == null", _enum != null);
			while (_enum.hasMore()) {

				// basic information about search result
				Object object = _enum.next();
				assertTrue("searchResult is wrong class",
						object instanceof SearchResult);
				SearchResult searchResult = (SearchResult) object;
				String name = searchResult.getName();
				assertTrue("name == null", name != null);

				// print attributes
				Attributes attributes = searchResult.getAttributes();
				NamingEnumeration attrEnum = attributes.getAll();
				System.out.println("attributes:");
				while (attrEnum.hasMore()) {
					Attribute attr = (Attribute) attrEnum.next();
					System.out.println(attr.getID() + " " + ", values:");
					NamingEnumeration attributeEnum = attr.getAll();
					while (attributeEnum.hasMore()) {
						System.out.println("\t" + attributeEnum.next());
					}
				}

			}
		} finally {
			try {
				if (context != null) {
					context.close();
				}
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Throws a RuntimeException if the assertion is false.
	 * 
	 * @param message
	 *            The interpretation if the assertion is false
	 * @param assertion
	 *            the assert to check
	 */
	private void assertTrue(String message, boolean assertion) {
		if (!assertion) {
			throw new RuntimeException(message);
		}
	}

	/**
	 * Gets a DirContext object based on the properties in the test.properties
	 * file.
	 * 
	 * @return representing a connection to the server.
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	private DirContext getDirContext() throws NamingException, IOException {
		Properties connectorProps = new Properties();
		InputStream is = new FileInputStream(PROPS);
		connectorProps.load(is);
		return new InitialDirContext(connectorProps);
	}

	/**
	 * Prints the command line useage to system out.
	 */
	private static void usage() {
		System.out.println("Usage");
		System.out.println("java DSML2DirContextTest OPTION");
		System.out.println("where OPTION is one of");
		System.out.println("--help  print this message");
		System.out.println("--op add  Adds a person to the directory.");
		System.out.println("--op DSE  Does a root DSE search.");
		System.out.println("--op lookup  Looks up a person to the directory.");
		System.out.println("--op mod  Modifies a person to the directory.");
		System.out.println("--op del  Deletes a person from the directory.");
	}

	/**
	 * This is the program entry point.
	 * 
	 * A single command line parameter lists the operation to perform. This is
	 * either '--help', which prints a help message, or '--op OPERATION', where
	 * operation is one of add, DSE, lookup, mod, del.
	 * 
	 * There may be system environment properties required if https is used as
	 * the transport.
	 * 
	 * @exception NamingException
	 *                passed on from the JNDI provider
	 * @exception IOException
	 *                may be generated in looking up configuration properties
	 */
	public static void main(String[] argv) throws NamingException, IOException {
		DSML2DirContextTest test = new DSML2DirContextTest();
		test.setUp();

		if ((argv.length == 0) || (argv[0].equals("--help"))) {
			usage();
		} else if (argv[1].equals("add")) {
			test.testCreateSubcontext();
		} else if (argv[1].equals("DSE")) {
			test.testSearchRootDSE();
		} else if (argv[1].equals("lookup")) {
			test.testLookup();
		} else if (argv[1].equals("mod")) {
			test.testModifyPerson();
		} else if (argv[1].equals("del")) {
			test.testDestroySubcontext();
		} else {
			usage();
		}
	}

}
