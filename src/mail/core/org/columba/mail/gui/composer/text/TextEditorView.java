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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.composer.text;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextPane;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.util.UndoDocument;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TextEditorView extends JTextPane implements Observer {

	private TextEditorController controller;
	private UndoDocument message;

	//	name of font
	private String name;

	// size of font
	private String size;

	//	currently used font
	private Font font;

	// font configuration
	private XmlElement textFontElement;

	private XmlElement fonts;

	// overwrite look and feel font settings
	private boolean overwrite;

	public TextEditorView(TextEditorController controller, UndoDocument m) {
		super();

		this.controller = controller;

		message = m;

		setStyledDocument(message);
		setEditable(true);

		/*
		Font font = Config.getOptionsConfig().getGuiItem().getTextFont();
		
		setFont(font);
		*/

		XmlElement options = Config.get("options").getElement("/options");
		XmlElement guiElement = options.getElement("gui");
		fonts = guiElement.getElement("fonts");
		if (fonts == null)
			fonts = guiElement.addSubElement("fonts");

		overwrite =
			new Boolean(fonts.getAttribute("overwrite", "true")).booleanValue();

		// register for configuration changes
		fonts.addObserver(this);

		textFontElement = fonts.getElement("text");
		if (textFontElement == null)
			textFontElement = fonts.addSubElement("text");

		if (!overwrite) {
			name = "Default";
			size = "12";

			font = new Font(name, Font.PLAIN, Integer.parseInt(size));

		} else {
			name = textFontElement.getAttribute("name", "Default");
			size = textFontElement.getAttribute("size", "12");

			font = new Font(name, Font.PLAIN, Integer.parseInt(size));
		}
		
		setFont(font);

		setPreferredSize(new Dimension(300, 200));
	}

	public void installListener(TextEditorController controller) {
		message.addDocumentListener(controller);
	}

	public void setCharset(String charset) {
		setContentType("text/plain; charset=\"" + charset + "\"");
	}

	/* (non-Javadoc)
		 * 
		 * @see org.columba.mail.gui.config.general.MailOptionsDialog
		 * 
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
	public void update(Observable arg0, Object arg1) {
		XmlElement e = (XmlElement) arg0;

		// fonts

		overwrite =
			new Boolean(fonts.getAttribute("overwrite", "true")).booleanValue();

		if (overwrite == false) {

			// use default font settings
			name = "Default";
			size = "12";

			font = new Font(name, Font.PLAIN, Integer.parseInt(size));

		} else {
			// overwrite look and feel font settings
			name = textFontElement.getAttribute("name", "Default");
			size = textFontElement.getAttribute("size", "12");

			font = new Font(name, Font.PLAIN, Integer.parseInt(size));
		}

		setFont(font);

	}

}