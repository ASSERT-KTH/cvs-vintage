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
import java.util.Observable;
import java.util.Observer;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.util.MailResourceLoader;

/**
 * Format selected text as strike out "<strike>" btw "<s>"
 *
 * @author fdietz
 */
public class StrikeoutFormatAction extends CheckBoxAction implements Observer {

	/**
	 * @param frameController
	 * @param name
	 */
	public StrikeoutFormatAction(AbstractFrameController frameController) {

		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_strike"));

		setTooltipText(
					MailResourceLoader.getString(
						"menu",
						"composer",
						"menu_format_strike_tooltip"));
						
		setLargeIcon(ImageLoader.getImageIcon("stock_text_strikethrough.png"));
		setSmallIcon(
			ImageLoader.getSmallImageIcon("stock_text_strikethrough-16.png"));

	}

	/**
		 * Method is called when text selection has changed.
		 * <p>
		 * Enable/Disable this actions on selection changes. 
		 * 
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
	public void update(Observable arg0, Object arg1) {
		Boolean isSelected = (Boolean) arg1;

		if (isSelected.equals(Boolean.TRUE)) {
			// text is selected
			setEnabled(true);
		} else {
			// no selection
			setEnabled(false);
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// this action is disabled when the text/plain editor is used
		// -> so, its safe to just cast to HtmlEditorController here
		HtmlEditorController editorController =
			(HtmlEditorController) ((ComposerController) frameController)
				.getEditorController();

		// TODO: implement this!
		//editorController.toggleStrikeout();

	}

}
