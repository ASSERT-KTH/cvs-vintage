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

import java.awt.Insets;
import java.io.StringReader;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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

	private static final String GLOBAL_CSS =
		"p {font-family:DialogInput; font-size:12pt}";
	private static final String CSS =
		"<style type=\"text/css\"><!--" + GLOBAL_CSS + "--></style>";

	private DocumentParser parser;

	private HTMLEditorKit htmlEditorKit;

	public BodyTextViewer() {
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);

		htmlEditorKit = new HTMLEditorKit();
		setEditorKit(htmlEditorKit);

		parser = new DocumentParser();	
		
		setContentType("text/html");
	}

	

	public void setBodyText(String bodyText, boolean html) {
		if (html) {
			try {
				
				String validated = parser.validateHTMLString(bodyText);

				/*
				File tempFile = TempFileStore.createTempFileWithSuffix("html");
				DiskIO.saveStringInFile(tempFile, validated);
				
				URL url = tempFile.toURL();
				
				setPage( url );
				*/
				
				//htmlEditorKit.write(new StringReader(validated),getDocument(),0,validated.length());
				//setText(validated);

				
				
				//getDocument().remove(0,getDocument().getLength()-1);
				
				
				
				
				((HTMLDocument) getDocument()).getParser().parse(
					new StringReader(validated),
					((HTMLDocument) getDocument()).getReader(0),
					true);
				
				
				//htmlEditorKit.read(new StringReader(validated), getDocument(),0);
				
				//setText(validated);
				//setText(bodyText);
				
				setCaretPosition(0);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			StringBuffer buf = new StringBuffer(bodyText);

			transformToHTML(buf);

			try {
				String r = parser.substituteURL(buf.toString());
				r = parser.substituteEmailAddress(r);
				setText(r);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			setCaretPosition(0);
		}
	}

	protected void transformToHTML(StringBuffer buf) {
		buf.insert(0, "<HTML><HEAD>" + CSS + "</HEAD><BODY ><P>");

		int pos = 0;
		boolean preformat = false;
		while (pos < buf.length()) {
			char ch = buf.charAt(pos);

			if (ch == '\n') {

				buf.replace(pos, pos + 1, "<br>");

				pos+=3;
			}

			pos++;
		}

		buf.append("</P></BODY></HTML>");

	}



}
