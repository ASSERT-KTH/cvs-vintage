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
import java.io.File;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.TempFileStore;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.message.util.DocumentParser;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BodyTextViewer extends JTextPane {


	// stylesheet is created dynamically because
	// user configurable fonts are used
	private String css = "";

	// parser to transform text to html
	private DocumentParser parser;

	private HTMLEditorKit htmlEditorKit;

	public BodyTextViewer() {
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);

		htmlEditorKit = new HTMLEditorKit();
		setEditorKit(htmlEditorKit);

		parser = new DocumentParser();

		setContentType("text/html");

		initStyleSheet();
	}

	/**
	 * 
	 * read text-properties from configuration and 
	 * create a stylesheet for the html-document 
	 *
	 */
	protected void initStyleSheet() {
		
		// read configuration from options.xml file
		XmlElement mainFont =
			Config.get("options").getElement("/options/gui/textfont");
		String name = mainFont.getAttribute("name");
		String size = mainFont.getAttribute("size");
		Font font = new Font(name, Font.PLAIN, Integer.parseInt(size));

		// create css-stylesheet string 
		// set font of html-element <P> 
		css =
			"<style type=\"text/css\"><!--p {font-family:\""
				+ name
				+ "\"; font-size:\""
				+ size
				+ "pt\"}--></style>";
	}

	public void setBodyText(String bodyText, boolean html) {
		if (html) {
			try {
				// this is a HTML message
				
				// try to fix broken html-strings
				String validated = parser.validateHTMLString(bodyText);
				ColumbaLogger.log.debug("validated bodytext:\n" + validated);

				// create temporary file
				File tempFile = TempFileStore.createTempFileWithSuffix("html");
				
				// save bodytext to file
				DiskIO.saveStringInFile(tempFile, validated);

			
				URL url = tempFile.toURL();
				
				// use asynchronous loading method setPage to display
				// URL correctly
				setPage(url);

				// this is the old method which doesn't work
				// for many html-messages
				/* 
				getDocument().remove(0,getDocument().getLength()-1);
								
				((HTMLDocument) getDocument()).getParser().parse(
					new StringReader(validated),
					((HTMLDocument) getDocument()).getReader(0),
					true);
				*/

				// scroll window to the beginning
				setCaretPosition(0);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// this is a text/plain message
			try {
				// substitute special characters like:
				//  <,>,&,\t,\n
				String r = parser.substituteSpecialCharacters(bodyText);
				
				// parse for urls and substite with HTML-code
				r = parser.substituteURL(r);
				// parse for email addresses and substite with HTML-code
				r = parser.substituteEmailAddress(r);
				
				
				// encapsulate bodytext in html-code
				r = transformToHTML(new StringBuffer(r));
				
				ColumbaLogger.log.debug("validated bodytext:\n" + r);

				// display bodytext
				setText(r);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// scroll window to the beginning
			setCaretPosition(0);
		}
	}

	/*
	 * 
	 * encapsulate bodytext in HTML code
	 * 
	 */
	protected String transformToHTML(StringBuffer buf) {
		// prepend
		buf.insert(0, "<HTML><HEAD>" + css + "</HEAD><BODY ><P>");
		// append
		buf.append("</P></BODY></HTML>");
		return buf.toString();
	}

}
