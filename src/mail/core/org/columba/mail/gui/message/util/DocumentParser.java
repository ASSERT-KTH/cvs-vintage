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

import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
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
	 * parse text and transform every email-address
	 * in a HTML-conform address
	 * 
	 */
	public String substituteEmailAddress(String s) throws Exception {
		//String pattern = "\\b(([\\w|.|\\-|_]*)@([\\w|.|\\-|_]*)(.)([a-zA-Z]{2,}))";

		// contributed by Paul Nicholls
		//  -> corrects inclusion of trailing full-stops
		//  -> works for numerical ip addresses, too
		String pattern = "([\\w.\\-]*\\@([\\w\\-]+\\.*)+[a-zA-Z0-9]{2,})";

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

	/*
		 * parse text and transform every url
		 * in a HTML-conform url
		 * 
		 */
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
			urlCompiler.compile(pattern, Perl5Compiler.CASE_INSENSITIVE_MASK);

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

	/*	
	 * 
	 * substitute special characters like:
	 *   <,>,&,\t,\n
	 * 
	 */
	public String substituteSpecialCharacters(String s) throws Exception {

		StringBuffer sb = new StringBuffer(s.length());
		StringReader sr = new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		String ss = null;

		/*
		 * *20030618, kpo* Changed the way multiple spaces are 
		 * replaced with &nbsp; so &nbsp; and spaces alternates - 
		 * allways ending with a space. This gives better wordwrap
		 * in e.g. the BodyTextViewer 
		 */

		try {

			while ((ss = br.readLine()) != null) {
				//for (int i = 0; i < ss.length(); i++) {
				int i = 0;
				while (i < ss.length()) {
					switch (ss.charAt(i)) {
						case '<' :
							sb.append("&lt;");
							i++;
							break;
						case '>' :
							sb.append("&gt;");
							i++;
							break;
						case '&' :
							sb.append("&amp;");
							i++;
							break;
						case ' ' :
							//sb.append("&nbsp;");
							if        (ss.substring(i).startsWith("    ")) {
								sb.append("&nbsp; ");
								i = i + 2;
							} else if (ss.substring(i).startsWith("   ")) {
								sb.append("&nbsp;&nbsp; ");
								i = i + 3;
							} else if (ss.substring(i).startsWith("  ")) {
								sb.append("&nbsp; ");
								i = i + 2;
							} else {
								sb.append(' ');
								i++;
							}
							break;
						case '\t' :
							sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
							i++;
							break;
						case '\n' :
							sb.append("<br>");
							i++;
							break;
						default :
							sb.append(ss.charAt(i));
							i++;
							break;
					}
				}
				sb.append("<br>\n");
			}

		} catch (Exception e) {
			System.out.print("Parsing Exception: " + e.getMessage());
		}

		return sb.toString();

	}
	
	/*	
		 * 
		 * substitute special characters like:
		 *   <,>,&,\t,\n
		 * 
		 * same as above method, but doesn't insert extra 
		 * newline character
		 * 
		 */
	public String substituteSpecialCharactersInHeaderfields(String s)
		throws Exception {

		StringBuffer sb = new StringBuffer(s.length());
		StringReader sr = new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		String ss = null;

		try {

			while ((ss = br.readLine()) != null) {
				for (int i = 0; i < ss.length(); i++) {
					switch (ss.charAt(i)) {
						case '<' :
							sb.append("&lt;");
							break;
						case '>' :
							sb.append("&gt;");
							break;
						case '&' :
							sb.append("&amp;");
							break;
						case '\t' :
							sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
							break;
						case '\n' :
							sb.append("<br>");
							break;
						default :
							sb.append(ss.charAt(i));
							break;
					}
				}

			}

		} catch (Exception e) {
			System.out.print("Parsing Exception: " + e.getMessage());
		}

		return sb.toString();

	}

	/**
	 * Performs in large terms the reverse of
	 * substituteSpecialCharacters (though br tags are not 
	 * converted to newlines, this should be handled separately).
	 * More preciesly it changes special entities like
	 * amp, nbsp etc. to their real counter parts: &, space etc. 
	 * 	 
	 * @param	s	input string
	 * @return	output with special entities with their real counter parts
	 * @author  karlpeder, 20030615
	 */
	public String restoreSpecialCharacters(String s) {

		StringBuffer sb = new StringBuffer(s.length());
		StringReader sr = new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		String ss = null;

		try {

			while ((ss = br.readLine()) != null) {
				int pos = 0;
				while (pos < ss.length()) {
					char c = ss.charAt(pos);
					if (c == '&') {
						if 		  (ss.substring(pos).startsWith("&lt;")) {
							sb.append('<');
							pos = pos + 4;
						} else if (ss.substring(pos).startsWith("&gt;")) {
							sb.append('>');
							pos = pos + 4;
						} else if (ss.substring(pos).startsWith("&amp;")) {
							sb.append('&');
							pos = pos + 5;
						} else if (ss.substring(pos).startsWith("&quot;")) {
							sb.append('"');
							pos = pos + 6;
						} else if (ss.substring(pos).startsWith(
									"&nbsp;&nbsp;&nbsp;&nbsp;")) {
							sb.append('\t');
							pos = pos + 24;
						} else if (ss.substring(pos).startsWith("&nbsp;")) {
							sb.append(' ');
							pos = pos + 6;
						} else {
							// unknown special entity - just keep it as-is
							sb.append(c);
							pos++;
						}
					} else {
						sb.append(c);
						pos++;
					}
				}
				sb.append('\n');
			}

		} catch (Exception e) {
			System.out.print("Parsing Exception: " + e.getMessage());
		}

		return sb.toString();
	}

	/**
	 * Strips html tags. The method used is very simple:
	 * Everything between tag-start (&lt) and tag-end (&gt) is removed.
	 * Optionaly br tags are replaced by newline and ending p tags with
	 * double newline.
	 * 
	 * @param	s				input string
	 * @param	insertNewlines	if true, newlines are inserted for br and p tags
	 * @return	output without html tags
	 * @throws  Exception
	 * @author	karlpeder, 20030615
	 */
	public String stripHTMLTags(String s, boolean insertNewlines) {

		PatternMatcher matcher   = new Perl5Matcher();
		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern;
		String pat;
		
		try {
			if (insertNewlines) {
				// replace <br> and </br> with newline
				pat = "\\<[/]?br\\>";
				pattern = compiler.compile(
						pat,
						Perl5Compiler.CASE_INSENSITIVE_MASK);
				s = Util.substitute(matcher, pattern,
						new Perl5Substitution("\n"), s, 
						Util.SUBSTITUTE_ALL);
				// replace </p> with double newline
				pat = "\\</p\\>";
				pattern = compiler.compile(
						pat,
						Perl5Compiler.CASE_INSENSITIVE_MASK);
				s = Util.substitute(matcher, pattern,
						new Perl5Substitution("\n\n"), s, 
						Util.SUBSTITUTE_ALL);
			}
			
			// strip tags
			pat = "\\<(.|\\n)*?\\>";
			pattern = compiler.compile(
					pat,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			s = Util.substitute(matcher, pattern,
					new Perl5Substitution(""), s, 
					Util.SUBSTITUTE_ALL);

		} catch (MalformedPatternException e) {
			System.out.print("Parsing Exception: " + e.getMessage());
		}

		return s;
	}
	
	/*
	 * 
	 * try to fix broken html-strings
	 *
	 */
	public String validateHTMLString(String input) {
		StringBuffer output = new StringBuffer(input);
		int index = 0;

		String lowerCaseInput = input.toLowerCase();

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

		// remove characters after </html> tag
		index = lowerCaseInput.indexOf("</html>");
		if (lowerCaseInput.length() >= index + 7) {
			lowerCaseInput = lowerCaseInput.substring(0, index + 7);
		}

		return output.toString();
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
