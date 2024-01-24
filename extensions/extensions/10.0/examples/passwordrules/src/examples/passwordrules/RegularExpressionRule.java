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

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;

import com.ibm.passwordrules.IncompatibleRulesException;
import com.ibm.passwordrules.InvalidPasswordException;
import com.ibm.passwordrules.LexicalRule;
import com.ibm.passwordrules.PasswordGenerator;
import com.ibm.passwordrules.Rule;
import com.ibm.passwordrules.ValidationInfo;

/**
 * TODO Maybe rewrite this using Java 1.4 RegEx?
 */
public class RegularExpressionRule extends LexicalRule {

	private static final long serialVersionUID = -8090279911935005529L;

	String m_regExp = null;

	REProgram m_compiledExp = null;

	/** Creates a new instance of RegularExpressionRule */
	public RegularExpressionRule() {
	}

	/**
	 * Constrain the generation template to values we know are valid.
	 * 
	 * @param generator
	 *            including a character template to be constrained.
	 * 
	 */
	public void constrain(PasswordGenerator generator) {
		try {
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Gets the constraining parameter set on a rule object
	 * 
	 * @return parameter String representing the parameter understood by this
	 *         rule - a regular expression.
	 * 
	 * 
	 */
	public String getParameter() {
		return m_regExp;
	}

	/**
	 * Try to join the specified rule with this one. The more restrictive rule
	 * takes precedence.
	 * 
	 * @param rule -
	 *            The rule to be joined with this one.
	 * 
	 * @return true - Returns true, if the specified rule was successfully
	 *         combined with this one. Returns false, if the specified rule and
	 *         this one are not of the same class\type or they are not dependent
	 *         on each other due to which they cannot be combined.
	 * 
	 * @throws IncompatibleRulesException -
	 *             Thrown if the specified rule and this one cannot be combined
	 *             because they are mutually exclusive, i.e. a password cannot
	 *             satisfy both the rules at the same time. Example if the given
	 *             rule specifies that the max length of password should be 5,
	 *             and if this rule specifies that the minimum length of
	 *             password should be 6, then this function may throw
	 *             IncompatibleRulesException.
	 */
	public boolean join(Rule rule) throws IncompatibleRulesException {

		if (rule instanceof RegularExpressionRule) {
			/*
			 * if( This rule and the given rule do not have their regular
			 * expressions as mutually exclusive) { combine logic
			 */
			return true;
			/*
			 * } else { // This one and given rule are mutually exclusive.
			 * Example one // this one may have RegularExpression as "a*" and
			 * the other // may have "b*". throw new
			 * IncompatibleRulesException(this, rule, "The rules are mutually
			 * exclusive"); }
			 */
		}
		return false;
	}

	/**
	 * Sets a parameter on a rule object
	 * 
	 * @param parameter
	 *            String representing the parameter understood by this rule - a
	 *            regular expression.
	 * 
	 */
	public void setParameter(String parameter) {
		m_regExp = parameter;
		try {
			RECompiler rec = new RECompiler();
			m_compiledExp = rec.compile(m_regExp);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Validate the given password using the given validation info. Returns true
	 * if the password validates (throws an exception otherwise).
	 * 
	 * @param validationInfo
	 *            contains contextual information about the password being
	 *            validated.
	 * @param password
	 *            Password to validate.
	 * @exception InvalidPasswordException
	 *                thrown when password is invalid.
	 * @return true if password is valid
	 * 
	 * 
	 */
	public boolean validate(String password, ValidationInfo validationInfo)
			throws InvalidPasswordException {
		boolean res = false;
		RE re = new RE(m_compiledExp);
		res = re.match(password);
		if (!res)
			throw new InvalidPasswordException(this, "Password (" + password
					+ ") doesn't match regular expression: " + m_regExp);
		return true;
	}

	/**
	 * Method is specifically for combining rules of same type
	 */
	public boolean cumulate(Rule rule) throws IncompatibleRulesException {
		
		return false;
	}

}
