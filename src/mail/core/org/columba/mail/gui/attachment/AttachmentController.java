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

package org.columba.mail.gui.attachment;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.columba.core.util.SwingWorker;
import org.columba.mail.gui.attachment.action.AttachmentActionListener;
import org.columba.mail.gui.attachment.menu.AttachmentMenu;
import org.columba.mail.gui.attachment.util.IconPanel;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.mimetype.MimeTypeViewer;
import org.columba.mail.gui.table.MessageSelectionListener;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * This class shows the attachmentlist
 *
 *
 * @version 1.0
 * @author Timo Stich
 */

public class AttachmentController implements MessageSelectionListener {

	public JScrollPane scrollPane;
	private boolean ready = true;
	private SwingWorker worker;

	private IconPanel attachmentPanel;

	//private int actIndex;
	private Object actUid;

	//private TempFolder subMessageFolder;

	private boolean inline;

	private AttachmentMenu menu;
	private AttachmentActionListener actionListener;

	private AttachmentView view;
	private AttachmentModel model;
	//private SelectionManager selectionManager;

	private AttachmentSelectionManager attachmentSelectionManager;

	private MailFrameController mailFrameController;

	public AttachmentController(MailFrameController superController) {
		super();

		this.mailFrameController = superController;

		model = new AttachmentModel();

		view = new AttachmentView(model);

		attachmentSelectionManager = new AttachmentSelectionManager();

		actionListener = new AttachmentActionListener(this);

		menu = new AttachmentMenu(this);

		getView().setDoubleClickActionCommand("OPEN");
		getView().addActionListener(actionListener);

		MouseListener popupListener = new PopupListener();
		getView().addMouseListener(popupListener);

		/*
		attachmentPanel = new IconPanel();
		attachmentPanel.setDoubleClickActionCommand("OPEN");
		attachmentPanel.addActionListener(getActionListener());
		attachmentPanel.addMouseListener(popupListener);
		*/

		/*
			scrollPane = new JScrollPane(attachmentPanel);
			scrollPane.getViewport().setBackground(Color.white);
			//attachmentPanel.setPreferredSize(new Dimension(600, 50));
			 */
	}

	public MailFrameController getFrameController()
	{
		return mailFrameController;
	}
		
	public AttachmentSelectionManager getAttachmentSelectionManager() {
		return attachmentSelectionManager;
	}

	/*
	public void setSelectionManager(SelectionManager m) {
		this.selectionManager = m;
	
		//selectionManager.addMessageSelectionListener(this);
	}
	*/

	public void messageSelectionChanged(Object[] newUidList) {
		System.out.println(
			"attachment-controller: received new message-selection changed event");

	}

	public AttachmentView getView() {

		return view;
	}

	public AttachmentModel getModel() {
		return model;
	}

	public AttachmentActionListener getActionListener() {
		return actionListener;
	}

	public boolean setMimePartTree(MimePartTree collection) {
		return getView().setMimePartTree(collection);

	}

	public void openWith(MimePart part, File tempFile) {
		MimeHeader header = part.getHeader();

		MimeTypeViewer viewer = new MimeTypeViewer();
		viewer.openWith(header, tempFile);

	}

	public void open(MimePart part, File tempFile) {
		MimeHeader header = part.getHeader();

		if (header.getContentType().toLowerCase().indexOf("message") != -1) {
			inline = true;
			openInlineMessage(part, tempFile);
		} else {
			inline = false;
			MimeTypeViewer viewer = new MimeTypeViewer();
			viewer.open(header, tempFile);
		}

	}

	protected void openInlineMessage(MimePart part, File tempFile) {

		/*
		subMessageFolder = new TempFolder();
		uid = null;
		
		Message subMessage = (Message) part.getContent();
		try {
			uid = subMessageFolder.add(subMessage, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		MainInterface.crossbar.operate(
			new GuiOperation(Operation.MESSAGEBODY, 4, subMessageFolder, uid));
		*/
	}

	private int countSelected() {
		return attachmentPanel.countSelected();
	}

	public int[] getSelection() {
		return attachmentPanel.getSelection();
	}

	private JPopupMenu getPopupMenu() {
		return menu.getPopupMenu();
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
				if (getView().countSelected() == 0) {
					getView().select(e.getPoint(), 0);
				}
				getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * Returns the mailFrameController.
	 * @return MailFrameController
	 */
	public MailFrameController getMailFrameController() {
		return mailFrameController;
	}

}