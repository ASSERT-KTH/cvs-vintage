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
package org.columba.mail.gui.message;

import java.awt.Font;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class HeaderViewer extends JTextPane {

	// background: ebebeb
	// frame: d5d5d5
	private static final String LEFT_COLUMN_PROPERTIES =
		"border=\"0\" nowrap font=\"dialog\" align=\"right\" valign=\"top\" width=\"65\"";
	private static final String RIGHT_COLUMN_PROPERTIES =
		"border=\"0\" align=\"left\" valign=\"top\" width=\"100%\"";
	private static final String OUTTER_TABLE_PROPERTIES =
		"border=\"1\" cellspacing=\"1\" cellpadding=\"1\" align=\"left\" width=\"100%\" style=\"border-width:1px; border-style:solid;  background-color:#ebebeb\"";
	private static final String INNER_TABLE_PROPERTIES =
		"border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" width=\"100%\"";
	private static final String GLOBAL_CSS =
		"td {font-family:Dialog; font-size:8pt}";
	private static final String CSS =
		"<style type=\"text/css\"><!--" + GLOBAL_CSS + "--></style>";

	String[] keys;

	DocumentParser parser;

	public HeaderViewer() {
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);

		HTMLEditorKit editorKit = new HTMLEditorKit();
		editorKit.setStyleSheet(initStyleSheet(editorKit));
		setEditorKit(editorKit);

		URL baseUrl = DiskIO.getResourceURL("org/columba/core/images/");
		ColumbaLogger.log.debug(baseUrl.toString());
		((HTMLDocument) getDocument()).setBase(baseUrl);

		parser = new DocumentParser();

		keys = new String[7];
		keys[0] = "Subject";
		keys[1] = "Date";
		keys[2] = "Reply-To";
		keys[3] = "From";
		keys[4] = "To";
		keys[5] = "Cc";
		keys[6] = "Bcc";

		//setFont(font);

	}

	protected StyleSheet initStyleSheet(HTMLEditorKit editorKit) {
		XmlElement mainFont =
			Config.get("options").getElement("/options/gui/mainfont");
		String name = mainFont.getAttribute("name");
		String size = mainFont.getAttribute("size");
		size = "12";
		Font font = new Font(name, Font.PLAIN, Integer.parseInt(size));

		StyleSheet style = editorKit.getStyleSheet();

		String s =
			"<style type=\"text/css\"><!--td {font-family:\""
				+ name
				+ "\"; font-size:\""
				+ size
				+ "pt\"}--></style>";
		style.addRule(s);

		return style;
	}

	void setHeader(HeaderInterface header, boolean hasAttachments)
		throws Exception {
		// border #949494
		// background #989898
		// #a0a0a0
		// bright #d5d5d5

		StringBuffer buf = new StringBuffer();
		buf.append(
			"<HTML><HEAD>"
				+ CSS
				+ "</HEAD><BODY ><TABLE "
				+ OUTTER_TABLE_PROPERTIES
				+ ">");
		for (int i = 0; i < keys.length; i++) {
			if (header.get(keys[i]) == null)
				continue;

			if (((String) header.get(keys[i])).length() == 0)
				continue;

			buf.append("<TR><TD " + LEFT_COLUMN_PROPERTIES + ">");
			buf.append("<B>" + keys[i] + " : </B></TD>");

			buf.append("<TD " + RIGHT_COLUMN_PROPERTIES + ">");
			buf.append(
				" "
					+ parser.substituteEmailAddress((String) header.get(keys[i]))
					+ "</TD>");

			buf.append("</TR>");
		}

		if (hasAttachments) {
			buf.append("<TR><TD " + LEFT_COLUMN_PROPERTIES + ">");

			buf.append("<IMG SRC=\"stock_attach.png\"></TD>");

			buf.append("<TD " + RIGHT_COLUMN_PROPERTIES + ">");
			buf.append(" " + "</TD>");
		}

		buf.append("</TABLE></BODY></HTML>");

		setText(buf.toString());

	}

	/*
	protected String parseHeader(String headerField, String value) {
	
		boolean addressList = false;
	
		for (int i = 2; i < keys.length; i++) {
			if (headerField.equalsIgnoreCase(keys[i]))
				addressList |= true;
		}
	
		if (addressList) {
			Vector v = ListParser.parseString(value);
			StringBuffer buf = new StringBuffer();
	
			for (int i = 0; i < v.size(); i++) {
				String s = ((String) v.get(i)).trim();
	
				buf.append("<A href=\"" + s + "\">" + s + "</A>");
				if (i != v.size() - 1)
					buf.append(" ,");
			}
	
			return buf.toString();
		} else
			return value;
	
	}
	*/
}
