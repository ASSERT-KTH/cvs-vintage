// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.parser;

import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.OptionsXmlConfig;
import org.columba.core.io.DiskIO;
import org.columba.main.MainInterface;
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
		this.config = MainInterface.config.getOptionsConfig();
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

		output = getValue(input.contentType, input.contentSubtype, "viewer");

		return output;
	}

	public String getViewer(String contentType, String contentSubtype) {
		String output;

		output = getValue(contentType, contentSubtype, "viewer");

		return output;
	}

	public void setViewer(MimeHeader input, String viewer) {
		setViewer(input.contentType, input.contentSubtype, viewer);
	}

	private String getValue(String contentType, String subType, String key) {

		AdapterNode section = config.getMimeTypeNode();

		AdapterNode actMime = null;
		AdapterNode type = null;

		for (int i = 0; i < section.getChildCount(); i++) {
			actMime = section.getChild(i);

			type = actMime.getChild("type");

			if (type.getValue().equals(contentType))
				break;
			actMime = null;
		}
		if (actMime == null) {
			return null;
		}

		String output = null;

		AdapterNode outputNode = actMime.getChild(key);
		if (outputNode != null)
			output = outputNode.getValue();

		AdapterNode subMime;
		AdapterNode subParserNode;

		for (int i = 0; i < actMime.getChildCount(); i++) {
			subMime = actMime.getChild(i);

			//System.out.println("i="+i+ " -> " + subMime.getName() );

			if (subMime.getName().equals("subtype")) {
				if (subMime.getChild("type").getValue().equals(subType)) {
					subParserNode = subMime.getChild(key);
					if (subParserNode != null)
						output = subParserNode.getValue();
				}
			}
		}

		return output;
	}

	public void setViewer(String contentType, String subType, String value) {
		AdapterNode section = config.getMimeTypeNode();

		AdapterNode actMime = null;
		AdapterNode type = null;

		for (int i = 0; i < section.getChildCount(); i++) {
			actMime = section.getChild(i);

			type = actMime.getChild("type");

			if (type.getValue().equals(contentType))
				break;
			actMime = null;
		}
		if (actMime == null) {
			actMime = section.addElement(config.createElementNode("mimetype"));
			actMime.addElement(
				config.createTextElementNode("type", contentType));
			actMime.addElement(config.createTextElementNode("viewer", value));

			return;
		}

		AdapterNode subMime;
		AdapterNode viewerNode;

		for (int i = 0; i < actMime.getChildCount(); i++) {
			subMime = actMime.getChild(i);

			if (subMime.getName().equals("subtype")) {
				if (subMime.getChild("type").getValue().equals(subType)) {
					viewerNode = subMime.getChild("viewer");
					if (viewerNode != null) {
						viewerNode.setValue(value);
						return;
					}

					subMime.addElement(
						config.createTextElementNode("viewer", value));
					return;
				}
			}
		}
		subMime = actMime.addElement(config.createElementNode("subtype"));
		subMime.addElement(config.createTextElementNode("type", subType));
		subMime.addElement(config.createTextElementNode("viewer", value));
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
							
					parserTable.put( parser.getRegisterString(), parser);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}