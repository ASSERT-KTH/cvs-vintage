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

import java.lang.reflect.Array;
import java.util.Hashtable;

import org.columba.core.config.Config;
import org.columba.core.config.OptionsXmlConfig;
import org.columba.core.xml.XmlElement;
import org.columba.mail.message.MimeHeader;

public class MimeRouter {

	private static final String parserPath =
		"org.columba.mail.parser.mimetypeparsers.";

	private static final String[] parsers =
		{ "MimeMultipartParser", "MimeMessageParser", };

	private Hashtable parserTable;

	private MimeTypeParser standardParser;
	private OptionsXmlConfig config;

	static private MimeRouter myInstance;

	private MimeRouter() {
		this.config = Config.getOptionsConfig();
		standardParser = new MimeStandardParser();

		parserTable = new Hashtable();
		loadAllParser();
	}

	/**
	 * Method getInstance
	 * This is instead of a public Constructor to implement the Singleton-Pattern for the MimeRouter.
	 * @return MimeRouter
	 */
	public static MimeRouter getInstance() {
		if (myInstance == null)
			myInstance = new MimeRouter();

		return myInstance;
	}

	/**
	 * Method getTypeParser.
	 * Use to get the Parser for a specific ContentType
	 * @param input
	 * MimeHeader to get parser for
	 * @return MimeTypeParser
	 */
	public MimeTypeParser getTypeParser(MimeHeader input) {
		// If no ContentType specified return StandardParser
		if (input.contentType == null)
			return standardParser;

		MimeTypeParser parser;

		// First try to find parser for "type/subtype"

		parser =
			(MimeTypeParser) parserTable.get(
				input.contentType + "/" + input.contentSubtype);
		if (parser != null) {
			return parser;
		}

		// Next try to find parser for "type"

		parser = (MimeTypeParser) parserTable.get(input.contentType);
		if (parser != null) {
			return parser;
		}

		// Nothing found -> return StandardParser
		return standardParser;
	}

	public String getViewer(MimeHeader input) {
		String output;

		output = getViewer(input.contentType, input.contentSubtype);

		return output;
	}

	public void setViewer(MimeHeader input, String viewer) {
		setViewer(input.contentType, input.contentSubtype, viewer);
	}

	public String getViewer(String contentType, String subType) {

		XmlElement mimetype = config.getMimeTypeNode();

		XmlElement type, subtype;

		for (int i = 0; i < mimetype.count(); i++) {
			type = mimetype.getElement(i);
			if (type.getAttribute("name").equals(contentType)) {
				// Search for subtype viewer
				for (int j = 0; j < type.count(); j++) {
					subtype = type.getElement(j);
					if (subtype.getAttribute("name").equals(subType)) {
						if (subtype.getAttribute("viewer") != null) {
							return subtype.getAttribute("viewer");
						}
					}
				}

				// No subtype viewer found ->return type viewer	
				return type.getAttribute("viewer");
			}
		}

		return null;
	}

	public void setViewer(String contentType, String subType, String value) {
		XmlElement mimetype = config.getMimeTypeNode();

		XmlElement type = null;
		XmlElement subtype = null;
		XmlElement temp;

		// Search for contentType node
		for (int i = 0; i < mimetype.count(); i++) {
			temp = mimetype.getElement(i);
			if (temp.getAttribute("name").equals(contentType)) {
				type = temp;
				break;
			}
		}

		// If no node is found create new one
		if (type == null) {
			type = new XmlElement("type");
			type.addAttribute("name", contentType);
			type.addAttribute("viewer", value);
			mimetype.addElement(type);			
		}
		// Set to default viewer for this type if none is present
		else if (type.getAttribute("viewer") == null) {
			type.addAttribute("viewer", value);
		}

		// Search for subtype node
		for (int j = 0; j < type.count(); j++) {
			temp = type.getElement(j);
			if (temp.getAttribute("name").equals(subType)) {
				subtype = temp;
			}
		}

		// No subtype node found -> create one
		if (subtype == null) {
			subtype = new XmlElement("subtype");
			subtype.addAttribute("name", subType);
			subtype.addAttribute("viewer", value);
			type.addElement(subtype);
		} else {
			subtype.addAttribute("viewer", value);
		}

	}

	private void loadAllParser() {
		Class actClass;
		ClassLoader loader = ClassLoader.getSystemClassLoader();

		try {
			for (int i = 0; i < Array.getLength(parsers); i++) {
				actClass = loader.loadClass(parserPath + parsers[i]);

				if (actClass
					.getSuperclass()
					.getName()
					.equals("org.columba.mail.parser.MimeTypeParser")) {

					MimeTypeParser parser =
						(MimeTypeParser) actClass.newInstance();

					parserTable.put(parser.getRegisterString(), parser);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}