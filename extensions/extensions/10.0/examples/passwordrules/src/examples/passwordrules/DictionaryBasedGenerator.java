/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2005, 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/

package examples.passwordrules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.ibm.passwordrules.PasswordGenerator;

public class DictionaryBasedGenerator implements PasswordGenerator {

	private static String[] s_dictionary = new String[] { "yellowsubmarine",
			"paperbackwriter", "ivegotafeeling", "gettingbetter",
			"eleonorrigby", "fixingahole", "rubbersoul", "yesterday",
			"revolver", "heyjude", "misery", "help!" };

	/** Creates a new instance of CustomGenerator */
	public DictionaryBasedGenerator() {
	}

	/**
	 * Method called by classes implementing Rule interface used to constrain
	 * the set of character templates to be used by the generator.
	 * 
	 * @param list
	 *            Array of characters to add to all the character templates.
	 */
	public void addChars(char[] list) {
		throw new RuntimeException(this.getClass().getName()
				+ " doesn't support addChars method");
	}

	/**
	 * Method called by classes implementing Rule interface used to constrain
	 * the set of character templates to be used by the generator to contain
	 * only lower case characters.
	 */
	public void allLowerCase() {
		for (int i = 0; i < s_dictionary.length; i++) {
			String w = s_dictionary[i];
			s_dictionary[i] = w.toLowerCase();
		}
	}

	/**
	 * Method called by classes implementing Rule interface used to constrain
	 * the set of character templates to be used by the generator to contain
	 * only upper case characters.
	 */
	public void allUpperCase() {
		for (int i = 0; i < s_dictionary.length; i++) {
			String w = s_dictionary[i];
			s_dictionary[i] = w.toUpperCase();
		}
	}

	/**
	 * Method will generate a string containing a password.
	 * 
	 * @return password
	 */
	public String generate() {
		Random r = new Random();
		int w = r.nextInt(s_dictionary.length);
		return s_dictionary[w];
	}

	/**
	 * Method called by classes implementing Rule interface used to constrain
	 * the set of character templates to be used by the generator.
	 * 
	 * @param numChars
	 *            Number of character templates to increase by
	 */
	public void setLength(int length) {
	}

	/**
	 * Method called by classes implementing Rule interface used to constrain
	 * the set of character templates to be used by the generator.
	 * 
	 * @param charList
	 *            list of characters to remove from all the character templates.
	 */
	public void removeChars(char[] charList) {
		List<String> newprefixes = new ArrayList<String>(s_dictionary.length);
		for (int i = 0; i < s_dictionary.length; i++) {
			String w = s_dictionary[i];
			for (int j = 0; j < charList.length; j++) {
				char c = charList[j];
				if (w.indexOf(c) < 0) {
					newprefixes.add(w);
					break;
				}
			}
		}
		Iterator iter = newprefixes.iterator();
		s_dictionary = new String[newprefixes.size()];
		for (int j = 0; iter.hasNext(); j++) {
			String w = (String) iter.next();
			s_dictionary[j] = w;
		}
	}

	public void setCharListAt(char[] charList, int startIndex, int endIndex) {
	}

	/**
	 * Method will set a parameter, which may provide hints to the password
	 * generator at runtime.
	 */
	public void initialize(String parameter) {
	}

	public void addAllowedCharSet(Set<char[]> arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

}
