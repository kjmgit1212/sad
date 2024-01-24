header {
package examples.serviceprovider.rdbms.parser;

import java.util.*;

import com.ibm.itim.logging.SystemLog;

import com.ibm.itim.common.*;
}  

/**
 * Lexer for SQL substitution language used in service forms.
 */
class SQLSubstitutionLexer extends Lexer;
options {
    // Lookahead of 2
    k = 2;
	// For unicode, allow any char but \uFFFF (16 bit -1)
	charVocabulary='\u0000'..'\uFFFE';
}

// definition of substitution variable
// substitute for supplied variables
VAR
	:	'#' ('a'..'z'|'A'..'Z'|'_'|'#') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'#')*
	;

// copy without modification
IGNORE
  :  . 
  ;


   