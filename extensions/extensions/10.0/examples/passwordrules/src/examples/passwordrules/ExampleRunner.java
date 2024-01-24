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

package examples.passwordrules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import com.ibm.itim.dataservices.model.policy.PasswordPolicy;
import com.ibm.passwordrules.standard.RuleSet;

public class ExampleRunner {

	public static final String PASSWORD_RULES_XML_FILE = "PasswordRuleSet.xml";

	FileInputStream m_fis = null;

	File m_prFile = null;

	/** Creates a new instance of ExampleRunner */
	public ExampleRunner() {
	}

	public void setUp() throws FileNotFoundException, IOException {
		URL fileURL = this.getClass().getResource(PASSWORD_RULES_XML_FILE);
		m_prFile = new File(URLDecoder.decode(fileURL.getFile(), "UTF-8"));
		m_fis = new FileInputStream(m_prFile);

	}

	public void tearDown() throws IOException {
		if (m_fis != null) {
			m_fis.close();
		}
	}

	public static final void main(String[] args) {
		ExampleRunner runner = new ExampleRunner();
		try {
			runner.setUp();
			runner.generatePassword();
		} catch (Throwable th) {
			th.printStackTrace();
		} finally {
			try {
				runner.tearDown();
			} catch (Exception e) {
			}
		}
	}

	public void generatePassword() {
		try {
			RuleSet rs = new RuleSet(PasswordPolicy.loadFromXML(m_fis));
			String pwd = rs.generate();
			System.out.println("generated password: " + pwd);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
