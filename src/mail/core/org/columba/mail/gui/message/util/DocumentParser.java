//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.message.util;

import org.apache.oro.text.regex.MalformedPatternException;
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

	PatternCompiler quotingCompiler;
	Pattern quotingPattern;
	PatternMatcher quotingMatcher;

	PatternCompiler characterCompiler;
	Pattern characterPattern;
	PatternMatcher characterMatcher;

	private final static String[] smilyCode =
		{ ":-\\)", ":-\\(", ":-\\|", ";-\\)" };
	private final static String[] smilyImage =
		{ "smile.gif", "sad.gif", "normal.gif", "wink.gif" };
	public DocumentParser() {

	}

	/*
	 * 
	 * make quotes font-color darkgray
	 *
	 */
	public String markQuotings(String input) throws Exception {

		//String pattern = "\\n[ \\t]*(&gt;(([^\\n])*))";
		String pattern = "(^(&nbsp;)*&gt;[^\\n]*)|\\n((&nbsp;)*&gt;[^\\n]*)";

		quotingCompiler = new Perl5Compiler();
		quotingPattern =
			quotingCompiler.compile(
				pattern,
				Perl5Compiler.CASE_INSENSITIVE_MASK);

		quotingMatcher = new Perl5Matcher();

		String r = Util.substitute(quotingMatcher, quotingPattern,
			//new Perl5Substitution("\n<quote class=\"bodytext\" style=\"color:#949494;\">$1</quote>"),
	new Perl5Substitution("\n<font class=\"quoting\">$1$3</font>"),
		input,
		Util.SUBSTITUTE_ALL);
		return r;

	}

	public String addSmilies(String input) throws Exception {

		for (int i = 0; i < smilyCode.length; i++)
			input = replaceStringWithImage(input, smilyCode[i], smilyImage[i]);

		return input;
	}

	private String replaceStringWithImage(
		String input,
		String pattern,
		String image)
		throws MalformedPatternException {

		quotingCompiler = new Perl5Compiler();
		quotingPattern =
			quotingCompiler.compile(
				pattern,
				Perl5Compiler.CASE_INSENSITIVE_MASK);

		quotingMatcher = new Perl5Matcher();

		String r = Util.substitute(quotingMatcher, quotingPattern,
			//new Perl5Substitution("\n<quote class=\"bodytext\" style=\"color:#949494;\">$1</quote>"),
	new Perl5Substitution("<IMG SRC=\"" + image + "\">"),
		input,
		Util.SUBSTITUTE_ALL);
		return r;
	}

}
