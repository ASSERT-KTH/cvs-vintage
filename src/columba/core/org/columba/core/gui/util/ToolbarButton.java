// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.util;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import org.columba.core.action.BasicAction;
import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;

public class ToolbarButton extends JButton {
	String buttonText;

	static boolean WITH_ICON = true;
	static boolean WITH_TEXT = true;
	static boolean ALIGNMENT = true;

	public ToolbarButton()
	{
		
		super();
		setRequestFocusEnabled(false);
	}
	
	public ToolbarButton(Icon icon)
	{
		super(icon);
		setRequestFocusEnabled(false);
	}
	
	public ToolbarButton(BasicAction a) {
		super(a);

		setRequestFocusEnabled(false);
		setMargin(new Insets(1, 1, 1, 1));

		GuiItem item = Config.getOptionsConfig().getGuiItem();
		/*
		WindowItem item =
			MailConfig.getMainFrameOptionsConfig().getWindowItem();
		*/
		
		if (item.getBoolean("toolbar","enable_icon") == true)
			WITH_ICON = true;
		else
			WITH_ICON = false;

		if (item.getBoolean("toolbar","enable_text") == true)
			WITH_TEXT = true;
		else
			WITH_TEXT = false;

		if (item.getBoolean("toolbar","text_position") == true)
			ALIGNMENT = true;
		else
			ALIGNMENT = false;

		if ((WITH_ICON == true)
			&& (WITH_TEXT == true)
			&& (ALIGNMENT == true)) {

			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalTextPosition(SwingConstants.CENTER);
			setIcon(a.getLargeIcon());

			setText(a.getToolbarName());

		} else if (
			(WITH_ICON == true)
				&& (WITH_TEXT == true)
				&& (ALIGNMENT == false)) {
			setVerticalTextPosition(SwingConstants.CENTER);
			setHorizontalTextPosition(SwingConstants.RIGHT);
			setIcon(a.getLargeIcon());

			if (a.isShowToolbarText())
				setText(a.getName());
			else
				setText(null);

		} else if ((WITH_ICON == true) && (WITH_TEXT == false)) {

			setIcon(a.getLargeIcon());
			setText(null);

		} else if ((WITH_ICON == false) && (WITH_TEXT == true)) {

			setIcon(null);
			setText(a.getName());

		}
	}

	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

}