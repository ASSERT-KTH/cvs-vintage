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
package org.columba.mail.gui.composer.html.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.text.html.HTML;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.gui.composer.html.util.FormatInfo;
import org.columba.mail.util.MailResourceLoader;

/**
 * Submenu for formatting text. 
 * <p>
 * Possible values are:
 *  - normal 
 *  - preformatted
 *  - heading 1
 *  - heading 2
 *  - heading 3
 *  - address
 * 
 * Note: This is the place to add further formats like lists, etc.
 *
 * Note: The HtmlEditorView and -Controller must of course also support
 *       new formats when adding them!
 * 
 * @author fdietz, Karl Peder Olesen (karlpeder)
 */
public class ParagraphMenu extends IMenu
		implements Observer, ActionListener {

	ButtonGroup group;

	/** Supported paragraph styles */
	public final static String[] STYLES = {
			"Normal",
			"Preformatted",
			"Heading 1",
			"Heading 2",
			"Heading 3",
			"Address" };

	/** Html tags corresponding to supported paragraph styles */
	public final static HTML.Tag[] STYLE_TAGS = {			
			HTML.Tag.P,
			HTML.Tag.PRE,
			HTML.Tag.H1,
			HTML.Tag.H2,
			HTML.Tag.H3,
			HTML.Tag.ADDRESS };
	
	/**
	 * @param controller
	 * @param caption
	 */
	public ParagraphMenu(AbstractFrameController controller) {
		super(
			controller,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_paragraph"));

		initMenu();
		
		// register for text selection changes
		((ComposerController) controller)
			.getEditorController()
			.addObserver(
			this);
		
		// default is disabled, since no text is selected on startup
		// ... will be enabled by the code in update
		setEnabled(false);		
	}

	/** 
	 * Initializes the sub menu by creating a menu item for each 
	 * available paragraph style. All menu items are grouped in a 
	 * ButtonGroup (as radio buttons).
	 */
	protected void initMenu() {
		group = new ButtonGroup();

		for (int i = 0; i < STYLES.length; i++) {
			JRadioButtonMenuItem m = new JRadioButtonMenuItem(STYLES[i]);
			m.setActionCommand(STYLE_TAGS[i].toString());
			m.addActionListener(this);
			add(m);

			group.add(m);
		}
	}

	/**
	 * Method is called when text selection has changed.
	 * <p>
	 * Set state of togglebutton / -menu to pressed / not pressed
	 * when selections change. 
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
		
		if (arg1 == null) {
			return; 
		}
	
		// check if text is selected
		// If not, paragraph formatting can not be set
		FormatInfo info = (FormatInfo) arg1;
		setEnabled(info.isTextSelected());
		
		// select the menu item corresponding to present format
		
		if        (info.isHeading1()) {
			selectMenuItem(HTML.Tag.H1);
		} else if (info.isHeading2()) {
			selectMenuItem(HTML.Tag.H2);
		} else if (info.isHeading3()) {
			selectMenuItem(HTML.Tag.H3);
		} else if (info.isPreformattet()) {
			selectMenuItem(HTML.Tag.PRE);
		} else if (info.isAddress()) {
			selectMenuItem(HTML.Tag.ADDRESS);
		} else {
			// select the "Normal" entry as default
			selectMenuItem(HTML.Tag.P);
		}
				
	}
	
	/**
	 * Private utility to select a given sub menu, given the 
	 * corresponding html tag.
	 * If such a sub menu does not exist - nothing happens
	 */
	private void selectMenuItem(HTML.Tag tag) {
		
		Enumeration enum = group.getElements();
		while (enum.hasMoreElements()) {
			JRadioButtonMenuItem item = 
					(JRadioButtonMenuItem) enum.nextElement();
			if (item.getActionCommand().equals(tag.toString())) {
				item.setSelected(true);
				return;	// done
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		HtmlEditorController ctrl =	(HtmlEditorController)
				((ComposerController) controller).getEditorController();

		// set paragraph formatting according to the given action
		if 		  (action.equals(HTML.Tag.P.toString())) {
			// <p>
			ctrl.setFormatNormal();
			
		} else if (action.equals(HTML.Tag.PRE.toString())) {
			// <pre>

			// TODO: Implement <pre>

		} else if (action.equals(HTML.Tag.H1.toString())) {
			// <h1>
			ctrl.setFormatHeading(1);
			
		} else if (action.equals(HTML.Tag.H2.toString())) {
			// <h2>
			ctrl.setFormatHeading(2);
			
		} else if (action.equals(HTML.Tag.H3.toString())) {
			// <h3>
			ctrl.setFormatHeading(3);
			
		} else if (action.equals(HTML.Tag.ADDRESS.toString())) {
			// <address>
			
			// TODO: Implement <address>
		}

	}

}
