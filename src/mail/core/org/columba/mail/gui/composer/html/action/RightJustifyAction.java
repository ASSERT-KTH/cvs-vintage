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

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

/**
 * Left justify selected paragraph "align="right" in <p>-tag
 *
 * TODO: Add actionPerformed etc. etc.
 * 
 * @author fdietz
 */
public class RightJustifyAction extends CheckBoxAction {

	/**
	 * @param frameController
	 * @param name
	 */
	public RightJustifyAction(AbstractFrameController frameController) {

		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_right_justify"));

		setTooltipText(
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_format_right_justify_tooltip"));

		setLargeIcon(ImageLoader.getImageIcon("stock_text_align_right.png"));
		setSmallIcon(
			ImageLoader.getSmallImageIcon("stock_text_align_right-16.png"));

		// TODO: Can be enabled when implemented
		
		setEnabled(false);
	}

}
