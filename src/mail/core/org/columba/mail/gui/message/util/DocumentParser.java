package org.columba.mail.gui.message.util;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DocumentParser {

	PatternCompiler urlCompiler;
	Pattern urlPattern;
	PatternMatcher urlMatcher;

	PatternCompiler addressCompiler;
	Pattern addressPattern;
	PatternMatcher addressMatcher;

	public String substituteEmailAddress(String s) throws Exception {
		String pattern = "\\b(([\\w|.|\\-|_]*)@([\\w|.|\\-|_]*)(.)([a-zA-Z]{2,}))";

		addressCompiler = new Perl5Compiler();
		addressPattern =
			addressCompiler.compile(
				pattern,
				Perl5Compiler.CASE_INSENSITIVE_MASK);

		addressMatcher = new Perl5Matcher();

		String result =
			Util.substitute(
				addressMatcher,
				addressPattern,
				new Perl5Substitution("<A HREF=mailto:$1>$1</A>"),
				s,
				Util.SUBSTITUTE_ALL);

		return result;
	}

	public String substituteURL(String s) throws Exception {
		String urls = "(http|https|ftp)";
		String letters = "\\w";
		String gunk = "/#~:.?+=&@!\\-%";
		String punc = ".:?\\-";
		String any = "${" + letters + "}${" + gunk + "}${" + punc + "}";

		/**
		 *
		 *  
		 * \\b  				start at word boundary
		 * (					begin $1
		 * urls:				url can be (http:, https:, ftp:) 
		 * [any]+?				followed by one or more of any valid character
		 * 						(be conservative - take only what you need)
		 * )					end of $1
		 * (?=					look-ahead non-consumptive assertion
		 * [punc]*				either 0 or more punctuation
		 * [^any]				  followed by a non-url char
		 * |					or else
		 * $					  then end of the string
		 * )
		 */
		String pattern =
			"\\b"
				+ "("
				+ urls
				+ ":["
				+ any
				+ "]+?)(?=["
				+ punc
				+ "]*[^"
				+ any
				+ "]|$)";

		urlCompiler = new Perl5Compiler();
		urlPattern =
			urlCompiler.compile(
				pattern,
				Perl5Compiler.CASE_INSENSITIVE_MASK);

		urlMatcher = new Perl5Matcher();

		String result =
			Util.substitute(
				urlMatcher,
				urlPattern,
				new Perl5Substitution("<A HREF=$1>$1</A>"),
				s,
				Util.SUBSTITUTE_ALL);

		return result;
	}
	
	
	public String validateHTMLString(String input) {
		StringBuffer output = new StringBuffer(input);
		int index = 0;

		String lowerCaseInput = input.trim();

		// Check for missing  <html> tag
		if (lowerCaseInput.indexOf("<html>") == -1) {
			if (lowerCaseInput.indexOf("<!doctype") != -1)
				index =
					lowerCaseInput.indexOf(
						"\n",
						lowerCaseInput.indexOf("<!doctype"))
						+ 1;
			output.insert(index, "<html>");
		}

		// Check for missing  </html> tag
		if (lowerCaseInput.indexOf("</html>") == -1) {
			output.append("</html>");
		}

		return output.toString();
	}
}
