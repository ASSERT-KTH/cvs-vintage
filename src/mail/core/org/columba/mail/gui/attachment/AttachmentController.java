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

package org.columba.mail.gui.attachment;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.gui.attachment.action.OpenAction;
import org.columba.mail.message.MimePartTree;

/**
 * This class shows the attachmentlist
 *
 *
 * @version 1.0
 * @author Timo Stich
 */

public class AttachmentController {

	public JScrollPane scrollPane;

	//private IconPanel attachmentPanel;

	//private int actIndex;
	//private Object actUid;

	//private TempFolder subMessageFolder;

	//private boolean inline;

	private AttachmentMenu menu;
	//private AttachmentActionListener actionListener;

	private AttachmentView view;
	private AttachmentModel model;

	private AbstractFrameController abstractFrameController;

	public AttachmentController(AbstractFrameController superController) {
		super();

		this.abstractFrameController = superController;

		model = new AttachmentModel();

		view = new AttachmentView(model);

		abstractFrameController.getSelectionManager().addSelectionHandler(
			new AttachmentSelectionHandler(view));

		getView().setDoubleClickAction(new OpenAction(superController));

		MouseListener popupListener = new PopupListener();
		getView().addMouseListener(popupListener);

	}

	public AbstractFrameController getFrameController() {
		return abstractFrameController;
	}

	public AttachmentView getView() {
		return view;
	}

	public AttachmentModel getModel() {
		return model;
	}

	public boolean setMimePartTree(MimePartTree collection) {
		return getView().setMimePartTree(collection);
	}

	public void createPopupMenu() {
		menu = new AttachmentMenu(getFrameController());
	}

	private JPopupMenu getPopupMenu() {

		return menu;
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (getView().countSelected() <= 1) {
					getView().select(e.getPoint(), 0);
				}
				if (getView().countSelected() >= 1)
					getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

}
