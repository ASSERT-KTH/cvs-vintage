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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DocumentParser {

	private final static String[] smilyImage =
		{ "smile.gif", "sad.gif", "normal.gif", "wink.gif" };
		
	private final static Pattern[] smilyPattern = {
		Pattern.compile(":-\\)"),
		Pattern.compile(":-\\("),
		Pattern.compile(":-\\|"),
		Pattern.compile(";-\\)"),
	};
		
	private static final Pattern markQuotingsPattern = Pattern.compile("(^(&nbsp;)*&gt;[^\\n]*)|\\n((&nbsp;)*&gt;[^\\n]*)", Pattern.CASE_INSENSITIVE);
		
	public DocumentParser() {

	}

	/*
	 * 
	 * make quotes font-color darkgray
	 *
	 */
	public static String markQuotings(String input) throws Exception {
		Matcher matcher = markQuotingsPattern.matcher(input);
		return matcher.replaceAll("\n<font class=\"quoting\">$1$3</font>");
	}

	public static String addSmilies(String input) throws Exception {
		Matcher matcher;

		for (int i = 0; i < smilyPattern.length; i++) {
			matcher = smilyPattern[i].matcher( input );
			input = matcher.replaceAll("<IMG SRC=\"" + smilyImage[i] + "\">");
		}

		return input;
	}

}
