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

package org.columba.mail.gui.attachment.menu;

import java.awt.event.MouseAdapter;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.attachment.action.AttachmentActionListener;

/**
 * menu for the tableviewer
 */

public class AttachmentMenu {
	private JPopupMenu popup;
	private AttachmentController attachmentViewer;

	public AttachmentMenu(AttachmentController attachmentViewer) {
		this.attachmentViewer = attachmentViewer;

		initPopupMenu();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	protected AttachmentActionListener getActionListener() {
		return attachmentViewer.getActionListener();
	}

	protected void initPopupMenu() {
		popup = new JPopupMenu();

		MouseAdapter statusHandler = attachmentViewer.getMailFrameController().getMouseTooltipHandler();
		org.columba.core.gui.util.CMenuItem menuItem;

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().openAction);
		menuItem.addMouseListener(statusHandler);

		popup.add(menuItem);

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().openWithAction);
		menuItem.addMouseListener(statusHandler);

		popup.add(menuItem);

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().saveAsAction);
		menuItem.addMouseListener(statusHandler);

		popup.add(menuItem);

		popup.add(new JSeparator());

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().viewHeaderAction);
		menuItem.addMouseListener(statusHandler);

		popup.add(menuItem);
	}

}
