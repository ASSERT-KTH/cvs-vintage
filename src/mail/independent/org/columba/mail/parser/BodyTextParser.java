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

package org.columba.mail.parser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;
import org.columba.core.logging.ColumbaLogger;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BodyTextParser {

	public static String quote( String text )
	{
		if (text == null)
			return null;


		StringBuffer buf = new StringBuffer();

		/*
		 * *20030621, karlpeder* The StringTokenizer treats
		 * \n\n as a single newline => empty lines
		 * are discharged. Therefore the StringTokenizer is
		 * no longer used.
		 */
/*
		StringTokenizer tok = new StringTokenizer(text, "\n");
		while (tok.hasMoreElements()) {
			buf.append("> ");
			buf.append((String) tok.nextElement());
			buf.append("\n");
		}
		return buf.toString();
*/
		int end = 0;
		int start = 0;
		while (end < text.length()) {
			end = text.indexOf('\n', start);
			if (end == -1) {
				end = text.length();
			}
			buf.append("> ");
			buf.append(text.substring(start, end));
			buf.append("\n");
			start = end + 1;
		}
		return buf.toString();
	}

	/**
	 * Strips html tags. and replaces special entities with their
	 * "normal" counter parts, e.g. &gt; => >.<br>
	 * The method used for stripping tags is very simple:
	 * Everything between tag-start (&lt) and tag-end (&gt) is removed.
	 * br tags are replaced by newline and ending p tags with
	 * double newline.
	 * 
	 * @param	html	input string
	 * @return	output without html tags and special entities (null on error)
	 * @author	karlpeder, 20030621
	 */
	public static String htmlToText(String html) {

		if (html == null)
			return null;

		PatternMatcher matcher   = new Perl5Matcher();
		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern;
		String pat;
		
		String text;
		
		// strip tags

		try {
			// replace <br> and </br> with newline
			pat = "\\<[/]?br\\>";
			pattern = compiler.compile(
					pat,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			text = Util.substitute(matcher, pattern,
					new Perl5Substitution("\n"), html, 
					Util.SUBSTITUTE_ALL);
			// replace </p> with double newline
			pat = "\\</p\\>";
			pattern = compiler.compile(
					pat,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			text = Util.substitute(matcher, pattern,
					new Perl5Substitution("\n\n"), text, 
					Util.SUBSTITUTE_ALL);
			
			// strip rest of tags
			pat = "\\<(.|\\n)*?\\>";
			//pat = "\\<(.)*?\\n";
			pattern = compiler.compile(
					pat,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			text = Util.substitute(matcher, pattern,
					new Perl5Substitution(""), text,
					Util.SUBSTITUTE_ALL);

		} catch (MalformedPatternException e) {
			ColumbaLogger.log.error(
				"Error converting html to text " + e.getMessage(), e);
			return null;
		}

		// convert special entities

		StringBuffer sb = new StringBuffer(text.length());
		BufferedReader br = new BufferedReader(new StringReader(text));
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
			ColumbaLogger.log.error(
				"Error converting html to text " + e.getMessage(), e);
			return null;
		}

		// return text stripped for tags and special entities
		return sb.toString();

	}


}
