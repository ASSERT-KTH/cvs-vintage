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

package org.columba.core.mimetype;

import org.columba.core.config.Config;
import org.columba.core.config.OptionsXmlConfig;
import org.columba.core.xml.XmlElement;
import org.columba.ristretto.message.MimeHeader;

/**
 * Easy wrapper for handling which external application should
 * be responsible for opening certain mimetypes.
 * <p>
 * This is used by the attachment viewer of the mail-component,
 * used for opening a webbrowser, etc.
 *
 * @author fdietz
 */
public class MimeRouter {

	private OptionsXmlConfig config;

	static private MimeRouter myInstance;

	private MimeRouter() {
		this.config = Config.getOptionsConfig();

	}

	/**
	 * Method getInstance This is instead of a public Constructor to implement
	 * the Singleton-Pattern for the MimeRouter.
	 * 
	 * @return MimeRouter
	 */
	public static MimeRouter getInstance() {
		if (myInstance == null)
			myInstance = new MimeRouter();

		return myInstance;
	}

	public String getViewer(MimeHeader input) {
		String output;

		output =
			getViewer(
				input.getMimeType().getType(),
				input.getMimeType().getSubtype());

		return output;
	}

	public void setViewer(MimeHeader input, String viewer) {

		setViewer(
			input.getMimeType().getType(),
			input.getMimeType().getSubtype(),
			viewer);
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

}
