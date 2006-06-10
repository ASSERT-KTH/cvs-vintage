// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.columba.core.resourceloader.GlobalResourceLoader;
/**
 * Used to parse display names, e.g. John Doe, John Q. Public, Dr. 
 * @author Rick Horowitz
 *
 */
public class NameParser {
	
	public static void main(String args[]) {
		String[] testNames = {"Dr. Richard K. Ellington", "Bob Jones", "Ms. Jill Hark", "Robert B. Smith, Esq.", "Johnson, PhD.", 
				"Sue & Gene Stark", "George", "", null};
		NameParser parser = NameParser.getInstance();
		for (int i = 0; i < testNames.length; i++) {
			String name = testNames[i];
			System.out.println(i +": " +parser.parseDisplayName(name).toString());
		}
	}

	private static final String RESOURCE_PATH = "org.columba.core.i18n.global";
	
	/** Salutations used at the beginning of a display name. */
//	private static final String[] SALUTATIONS = { "Mr.", "Mrs.", "Ms.", "Dr.", "Congressman", "Congresswoman", "Senator", "President",
//		"Chancellor", "Minister", "Prime Minister" };
	/** Titles used at the end of a display name. */
//	private static final String[] TITLES = { "PhD", "Ph.D.", "PhD.", "Esq.", "Esquire" };
	
	private static NameParser _instance;
	/**
	 * Get the singleton instance of NameParser
	 * @return NameParser instance.
	 */
	public static NameParser getInstance() {
		if (_instance == null)
			_instance = new NameParser();
		return _instance;
	}
	
	/** Salutations used at the beginning of a display name. */
	private final String[] SALUTATIONS;
	/** Titles used at the end of a display name. */
	private final String[] TITLES;
	
	/**
	 * Construct a singleton instance
	 */
	private NameParser() {
		
		// Initialize salutation strings
		List<String> salutations = new ArrayList<String>();
		String salutationsStr = GlobalResourceLoader.getString(RESOURCE_PATH, "global", "name_parser_salutations");
		StringTokenizer st = new StringTokenizer(salutationsStr, ",");
		while (st.hasMoreTokens()) {
			salutations.add(st.nextToken());
		}
		SALUTATIONS = salutations.toArray(new String[salutations.size()]);
		
		// Initialize title strings
		List<String> titles = new ArrayList<String>();
		String titlesStr = GlobalResourceLoader.getString(RESOURCE_PATH, "global", "name_parser_titles");
		st = new StringTokenizer(titlesStr, ",");
		while (st.hasMoreTokens()) {
			titles.add(st.nextToken());
		}
		TITLES = titles.toArray(new String[titles.size()]);
		
	}
	
	public Name parseDisplayName(String nameInputStr) {
		
		// Treat a null name input string the same as an empty input string
		if (nameInputStr == null)
			nameInputStr = "";
		
		String displayName = nameInputStr;
		String salutation = null;
		String firstName = null;
		String middleName = null;
		String lastName = null;
		String title = null;
		
		// Get the salutation if one is specified
		salutation = findSalutation(nameInputStr);
		if (salutation != null) {
			// Remove the salutation
			nameInputStr = nameInputStr.substring(salutation.length());
		}
		
		// Get the title if one is specified
		title = findTitle(nameInputStr);
		if (title != null) {
			// Remove the title and the preceding comma
			nameInputStr = nameInputStr.substring(0, nameInputStr.length() - title.length());
			nameInputStr = nameInputStr.substring(0, nameInputStr.lastIndexOf(','));
		}
		
		// Determine whether there are 1, 2, or 3 names specified. These names should be separated by spaces
		//	or commas. If a comma separates the first two names, assume that the last name is specified first, 
		//	Otherwise, assume the first name is specified first. Middle name is always specified after the first name.
		StringTokenizer st = new StringTokenizer(nameInputStr, ", ");
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}

		boolean commaSpecified = nameInputStr.indexOf(',') >= 0;
		if (tokens.size() == 0) {
			// Do nothing
		} else if (tokens.size() == 1) {
			// Assume last name only
			lastName = tokens.get(0);
		} else if (tokens.size() == 2) {
			if (commaSpecified) {
				lastName = tokens.get(0);
				firstName = tokens.get(1);
			} else {
				firstName = tokens.get(0);
				lastName = tokens.get(1);
			}
		} else if (tokens.size() == 3) {
			if (commaSpecified) {
				lastName = tokens.get(0);
				firstName = tokens.get(1);
				middleName = tokens.get(2);
			} else {
				firstName = tokens.get(0);
				middleName = tokens.get(1);
				lastName = tokens.get(2);
			}
		} else {
			// More than 3 tokens. Assume the last token is the last name and take the rest of the names as the first name.
			// This handles names like this: "Sue & Gene Stark".
			StringBuffer firstSB = new StringBuffer();
			lastName = (String)tokens.get(tokens.size()-1);
			firstName = nameInputStr.substring(0, nameInputStr.length() - lastName.length()).trim();
		}
		
		return new Name(displayName, salutation, firstName, middleName, lastName, title);
	}

	/**
	 * Find the title in the "nameInputStr", if one exists. Searches the salutations including in 
	 * NameParser.TITLES.
	 * @param nameInputStr The display name to search.
	 * @return The title, or null, if none is found.
	 */
	private String findTitle(String nameInputStr) {
		String title = null;
		for (String s : TITLES) {
			if (nameInputStr.endsWith(s)) {
				title = s;
				break;
			}
		}
		return title;
	}
	
	/**
	 * Find the salutation in the "nameInputStr", if one exists. Searches the salutations including in 
	 * NameParser.SALUTATIONS.
	 * @param nameInputStr The display name to search.
	 * @return The salutation, or null, if none is found.
	 */
	private String findSalutation(String nameInputStr) {
		String salutation = null;
		for (String s : SALUTATIONS) {
			if (nameInputStr.startsWith(s)) {
				salutation = s;
				break;
			}
		}
		return salutation;
	}
	
	/**
	 * The parsed name.
	 */
	public static class Name {
		private String _displayName;
		private String _firstName;
		private String _middleName;
		private String _lastName;
		private String _salutation;
		private String _title;
		public Name(String displayName, String salutation, String firstName, String middleName, String lastName, String title) {
			_displayName = displayName;
			_salutation = salutation;
			_firstName = firstName;
			_middleName = middleName;
			_lastName = lastName;
			_title = title;
		}
		/**
		 * @return the displayName
		 */
		public String getDisplayName() {
			return _displayName;
		}
		/**
		 * @return the first name
		 */
		public String getFirstName() {
			return _firstName;
		}
		/**
		 * @return the last name
		 */
		public String getLastName() {
			return _lastName;
		}
		/**
		 * @return the middle name
		 */
		public String getMiddleName() {
			return _middleName;
		}
		/**
		 * @return the salutation, e.g. Mr., Ms., Dr., etc.
		 */
		public String getSalutation() {
			return _salutation;
		}
		/**
		 * @return the title, e.g. PhD, Esquire, Senator, etc.
		 */
		public String getTitle() {
			return _title;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (_salutation != null) {
				sb.append(_salutation);
				sb.append(" ");
			}
			if (_firstName != null) {
				sb.append(_firstName);
				sb.append(" ");
			}
			if (_middleName != null) {
				sb.append(_middleName);
				sb.append(" ");
			}
			if (_lastName != null) {
				sb.append(_lastName);
			}
			if (_title != null) {
				sb.append(", ");
				sb.append(_title);
			}
			
			return sb.toString();
		}
	}

}
