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

package org.columba.mail.gui.composer;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.main.MainInterface;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.FileSource;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentController implements KeyListener, FocusOwner, ListSelectionListener {
	AttachmentView view;
	ComposerController controller;

	AttachmentActionListener actionListener;
	AttachmentMenu menu;

	private JFileChooser fileChooser;

	public AttachmentController(ComposerController controller) {
		this.controller = controller;

		view = new AttachmentView(this);
		
		actionListener = new AttachmentActionListener(this);

		menu = new AttachmentMenu(this);

		view.addPopupListener(new PopupListener());

		// register Component as FocusOwner
		MainInterface.focusManager.registerComponent(this);

		fileChooser = new JFileChooser();

		installListener();
		
		view.addListSelectionListener(this);

	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void installListener() {
		view.installListener(this);
	}

	public void updateComponents(boolean b) {
		if (b) {
			for (int i = 0;
				i < controller.getModel().getAttachments().size();
				i++) {
				StreamableMimePart p =
					(StreamableMimePart) controller.getModel().getAttachments().get(i);
				view.add(p);
			}
		} else {
			controller.getModel().getAttachments().clear();

			for (int i = 0; i < view.count(); i++) {
				StreamableMimePart mp = (StreamableMimePart) view.get(i);
				controller.getModel().getAttachments().add(mp);
			}
		}
	}

	public void add(StreamableMimePart part) {
		view.add(part);
		((ComposerModel) controller.getModel()).getAttachments().add(part);
	}

	public void removeSelected() {
		Object[] mp = view.getSelectedValues();
		for (int i = 0; i < mp.length; i++) {
			view.remove((StreamableMimePart) mp[i]);
		}

	}

	public void addFileAttachment() {
		int returnValue;
		File[] files;

		fileChooser.setDialogTitle(MailResourceLoader.getString("menu", "composer", "menu_message_attachFile")); //$NON-NLS-1$
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		returnValue = fileChooser.showOpenDialog(view);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			files = fileChooser.getSelectedFiles();

			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				FileNameMap fileNameMap = URLConnection.getFileNameMap();
				String mimetype = fileNameMap.getContentTypeFor(file.getName());
				if (mimetype == null)
					mimetype = "application/octet-stream"; //REALLY NEEDED?

				MimeHeader header =
					new MimeHeader(
						mimetype.substring(0, mimetype.indexOf('/')),
						mimetype.substring(mimetype.indexOf('/') + 1));
						
				header.putContentParameter("name", file.getName());
				header.setContentDisposition("attachment");
				header.putDispositionParameter("filename",file.getName());
				header.setContentTransferEncoding("base64");

				try {
					LocalMimePart mimePart =
						new LocalMimePart(header, new FileSource( file ));
					
					view.add(mimePart);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/******************* KeyListener ****************************/

	public void keyPressed(KeyEvent k) {

		switch (k.getKeyCode()) {
			case (KeyEvent.VK_DELETE) :
				delete();
				break;
		}
	}

	public void keyReleased(KeyEvent k) {
	}

	public void keyTyped(KeyEvent k) {
	}

	/********************** MouseListener *****************************/

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {

				Object[] values = view.getSelectedValues();
				if (values.length == 0)
					view.fixSelection(e.getX(), e.getY());

				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/********************** FocusOwner implementation *******************/

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#copy()
	 */
	public void copy() {
		// attachment controller doesn't support copy-operation
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#cut()
	 */
	public void cut() {
		if (view.count() > 0)
			removeSelected();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#delete()
	 */
	public void delete() {
		if (view.count() > 0)
			removeSelected();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#getComponent()
	 */
	public JComponent getComponent() {
		return view;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
	 */
	public boolean isCopyActionEnabled() {
		// attachment controller doesn't support copy actions

		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
	 */
	public boolean isCutActionEnabled() {
		if (view.getSelectedValues().length > 0)
			return true;

		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
	 */
	public boolean isDeleteActionEnabled() {
		if (view.getSelectedValues().length > 0)
			return true;

		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
	 */
	public boolean isPasteActionEnabled() {
		// attachment controller doesn't support paste actions
		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
	 */
	public boolean isSelectAllActionEnabled() {
		if (view.count() > 0)
			return true;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#paste()
	 */
	public void paste() {
		// attachment controller doesn't support paste actions

	}


	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
	 */
	public boolean isRedoActionEnabled() {
		// attachment controller doesn't support redo operation
		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
	 */
	public boolean isUndoActionEnabled() {
		// attachment controller doesn't support undo operation
		return false;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#redo()
	 */
	public void redo() {
		// attachment controller doesn't support redo operation

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#selectAll()
	 */
	public void selectAll() {
		view.setSelectionInterval(0, view.count()-1);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.focus.FocusOwner#undo()
	 */
	public void undo() {
		// attachment controller doesn't support undo operation

	}
	
	/********************* ListSelectionListener interface ***********************/
	
	

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent arg0) {
		MainInterface.focusManager.updateActions();

	}

}
