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
package org.columba.core.gui.toolbar;

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.base.ImageUtil;
import org.columba.core.help.HelpManager;
import org.columba.core.resourceloader.ImageLoader;

/**
 * Toolbar button factory.
 * 
 * @author fdietz
 * 
 */
public class ToolBarButtonFactory {

	protected static boolean WITH_TEXT = false;

	protected static boolean ALIGNMENT = true;

	public static JButton createAnimatedIconButton() {
		ImageSequenceTimer button = new ImageSequenceTimer();
		
		button.setRolloverEnabled(true);

		button.setRequestFocusEnabled(false);
		
		button.setMargin(new Insets(1, 1, 1, 1));
		
		return button;
	}

	public static JButton createButton(AbstractColumbaAction action) {
		JButton button = new JButton(action);

		button.setRolloverEnabled(true);

		button.setRequestFocusEnabled(false);
		
		button.setMargin(new Insets(1, 1, 1, 1));
		
		// JavaHelp support
		String topicID = (String) action
				.getValue(AbstractColumbaAction.TOPIC_ID);
		if (topicID != null) {
			HelpManager.getInstance().enableHelpOnButton(button, topicID);
		}

		GuiItem item = ((Config) Config.getInstance()).getOptionsConfig()
				.getGuiItem();

		WITH_TEXT = item.getBoolean("toolbar", "enable_text");
		ALIGNMENT = item.getBoolean("toolbar", "text_position");

		ImageIcon icon = (ImageIcon) action
				.getValue(AbstractColumbaAction.LARGE_ICON);
		if (icon == null) {
			// toolbar buttons always need an icon
			button.setIcon(ImageLoader.getImageIcon("brokenimage.png"));
		}
		
		if (icon != null) {
			button.setIcon(icon);

			// apply transparent icon
			button.setDisabledIcon(ImageUtil.createTransparentIcon(icon));
		}

		if (WITH_TEXT) {
			boolean showText = (action.isShowToolBarText() || ALIGNMENT);

			if (!showText) {
				button.setText("");
			} else {
				button.setText((String) action
						.getValue(AbstractColumbaAction.TOOLBAR_NAME));
			}

			if (ALIGNMENT) {
				button.setVerticalTextPosition(SwingConstants.BOTTOM);
				button.setHorizontalTextPosition(SwingConstants.CENTER);
			} else {
				button.setVerticalTextPosition(SwingConstants.CENTER);
				button.setHorizontalTextPosition(SwingConstants.RIGHT);
			}
		} else {
			button.setText(null);
		}

		return button;
	}
}
