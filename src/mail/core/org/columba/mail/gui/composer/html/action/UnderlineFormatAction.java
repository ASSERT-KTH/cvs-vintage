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

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.gui.composer.html.util.FormatInfo;
import org.columba.mail.util.MailResourceLoader;

/**
 * Format selected text as underline "<u>"
 *
 * @author fdietz
 */
public class UnderlineFormatAction extends CheckBoxAction implements Observer {

	/**
	 * @param frameController
	 * @param name
	 */
	public UnderlineFormatAction(AbstractFrameController frameController) {

		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_underline"));

		setTooltipText(
					MailResourceLoader.getString(
						"menu",
						"composer",
						"menu_format_underline_tooltip"));
						
		setLargeIcon(ImageLoader.getImageIcon("stock_text_underline.png"));
		setSmallIcon(
			ImageLoader.getSmallImageIcon("stock_text_underline-16.png"));

		// register for text selection changes
		((ComposerController) frameController)
			.getEditorController()
			.addObserver(
			this);
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
		
		// check if current text is bold or not - and set state accordingly
		FormatInfo info = (FormatInfo) arg1;
		boolean isUnderline = info.isUnderline();		

		// notify all observers to change their selection state
		getObservable().setSelected(isUnderline);
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

		editorController.toggleUnderline();

	}

}
