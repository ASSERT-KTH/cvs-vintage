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
package org.columba.mail.gui.message.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.menu.ExtendablePopupMenu;
import org.columba.core.gui.menu.MenuXMLDecoder;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.IMessageController;
import org.columba.mail.gui.message.action.OpenAttachmentAction;
import org.columba.mail.gui.message.action.SaveAsAttachmentAction;
import org.columba.mail.gui.message.command.SaveAttachmentTemporaryCommand;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.StreamableMimePart;
import org.frapuccino.iconpanel.IconPanel;
import org.frapuccino.swing.DynamicFileFactory;
import org.frapuccino.swing.DynamicFileTransferHandler;

/**
 * @author fdietz
 * 
 */

public class AttachmentsViewer extends IconPanel implements ICustomViewer {

	private MimeTree mimePartTree;

	private ExtendablePopupMenu menu;

	private AttachmentModel model;

	private IMessageController mediator;

	private MailFolderCommandReference ref;

	public AttachmentsViewer(IMessageController mediator) {
		super();

		this.mediator = mediator;

		model = new AttachmentModel();

		setOpaque(true);
		setBackground(UIManager.getColor("List.background"));

		setDragEnabled(true);
		setTransferHandler(new AttachmentTransferHandler(new FileGenerator()));

		MouseListener popupListener = new PopupListener();
		addMouseListener(popupListener);

		// set double-click action for attachment viewer
		setDoubleClickAction(new OpenAttachmentAction(mediator, this));
	}

	/**
	 * Sets the mime part. Adds icons to the view.
	 * 
	 * @param collection
	 *            collection containing mime parts.
	 * @return true if there was any mime parts added to the view; false
	 *         otherwise.
	 */
	private boolean setMimePartTree(MimeTree collection) {
		String contentType;
		String contentSubtype;
		String text = null;
		boolean output = false;

		removeAll();

		model.setCollection(collection);

		List displayedMimeParts = model.getDisplayedMimeParts();

		// Display resulting MimeParts
		for (int i = 0; i < displayedMimeParts.size(); i++) {
			StreamableMimePart mp = (StreamableMimePart) displayedMimeParts
					.get(i);

			MimeHeader header = mp.getHeader();
			MimeType type = header.getMimeType();

			contentType = type.getType();
			contentSubtype = type.getSubtype();

			// Get Text for Icon
			if (header.getFileName() != null) {
				text = header.getFileName();
			} else {
				text = contentType + "/" + contentSubtype;
			}

			// Get Tooltip for Icon
			StringBuffer tooltip = new StringBuffer();
			tooltip.append("<html><body>");

			if (header.getFileName() != null) {
				tooltip.append(header.getFileName());
				tooltip.append(" - ");
			}

			tooltip.append("<i>");

			if (header.getContentDescription() != null) {
				tooltip.append(header.getContentDescription());
			} else {
				tooltip.append(contentType);
				tooltip.append("/");
				tooltip.append(contentSubtype);
			}

			tooltip.append("</i></body></html>");

			ImageIcon icon = null;

			icon = new AttachmentImageIconLoader().getImageIcon(type.getType(),
					type.getSubtype());

			add(icon, text, tooltip.toString());

			output = true;
		}

		return output;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {

		setLocalReference(new MailFolderCommandReference(folder,
				new Object[] { uid }));

		mimePartTree = folder.getMimePartTree(uid);

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		if (mimePartTree == null)
			return;

		setMimePartTree(mimePartTree);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	private JPopupMenu getPopupMenu() {
		// bug #999990 (fdietz): make sure popup menu is created correctly
		if (menu == null) {
			menu = new ExtendablePopupMenu("mail.attachmentviewer");
			menu.add(new OpenAttachmentAction(mediator,
					this));
			menu.add(new SaveAsAttachmentAction(mediator,
					this));
		}

		return menu;
	}

	public void setLocalReference(MailFolderCommandReference ref) {
		this.ref = ref;
	}

	public MailFolderCommandReference getLocalReference() {
		Integer[] address = getSelectedMimePart().getAddress();
		ref.setAddress(address);

		return ref;
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

				if (countSelected() >= 1) {
					getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	private class FileGenerator implements DynamicFileFactory {

		/** {@inheritDoc} */
		public File[] createFiles(JComponent arg0) throws IOException {
			File[] files = new File[1];

			IMailFolderCommandReference ref = mediator.getSelectedReference();
			ref.setAddress(getSelected());
			
			SaveAttachmentTemporaryCommand command = new SaveAttachmentTemporaryCommand(
					ref);

			CommandProcessor.getInstance().addOp(command);

			command.waitForCommandToComplete();

			files[0] = command.getTempAttachmentFile();

			return files;
		}
	}

	/**
	 * Returns the selected mime part from the model.
	 * 
	 * @return the selected mime part.
	 */
	private StreamableMimePart getSelectedMimePart() {
		return (StreamableMimePart) model.getDisplayedMimeParts().get(
				getSelectedIndex());
	}

	public Integer[] getSelected() {
		StreamableMimePart mp = getSelectedMimePart();
		return mp.getAddress();
	}

	/**
	 * Transfer handler for the attachment view.
	 * 
	 * The Sun DnD integration with the the native platform, requires that the
	 * file is already exists on the disk, when a File DnD is issued. This
	 * TransferHandler will create the files locally when the
	 * 
	 * @linkplain java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 *            method is called. That method is the last method called before
	 *            the DnD has completed. The actual extraction is done through
	 *            the SaveAttachmentTemporaryCommand, and there might be
	 *            problems waiting for other commands to finish before it. The
	 *            method does not complete until the file has been created, ie
	 *            locks up the DnD action.
	 *            <p>
	 * @author redsolo
	 * @see org.columba.mail.gui.message.command.SaveAttachmentTemporaryCommand
	 * @see org.frappucino.swing.DynamicFileTransferHandler
	 */

	public class AttachmentTransferHandler extends DynamicFileTransferHandler {

		/**
		 * Setup the dynamic transfer handler.
		 * 
		 * @param factory
		 *            the factory that creates the file for the DnD action.
		 */
		public AttachmentTransferHandler(DynamicFileFactory factory) {
			super(factory, DynamicFileTransferHandler.LATE_GENERATION);
		}

		/**
		 * Returns the COPY action.
		 * 
		 * @param c
		 *            ignored.
		 * @return the
		 * @link TransferHandler#COPY COPY action.
		 */
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}

		/**
		 * Returns always false. The attachment transfer handler can only export
		 * data flavors, and not import them.
		 * 
		 * @param comp
		 *            ignored.
		 * @param transferFlavors
		 *            ignored.
		 * @return false.
		 */
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}
	}

	/**
	 * Imageloader using a content-type and subtype to determine the image name.
	 * <p>
	 * Automatically falls back to the default icon.
	 * 
	 * @author fdietz
	 */
	class AttachmentImageIconLoader {

		/**
		 * Utility constructor.
		 */
		private AttachmentImageIconLoader() {
		}

		/**
		 * Returns the image icon for the content type.
		 * 
		 * @param contentType
		 *            content type
		 * @param contentSubtype
		 *            content sub type
		 * @return an Image Icon for the content type.
		 */
		public ImageIcon getImageIcon(String contentType, String contentSubtype) {
			StringBuffer buf = new StringBuffer();
			buf.append("gnome-");
			buf.append(contentType);
			buf.append("-");
			buf.append(contentSubtype);
			buf.append(".png");

			ImageIcon icon = ImageLoader.getMimetypeIcon(buf.toString());

			if (icon == null) {
				icon = ImageLoader.getMimetypeIcon("gnome-" + contentType
						+ ".png");
			}

			if (icon == null) {
				icon = ImageLoader.getMimetypeIcon("gnome-text.png");
			}

			return icon;
		}
	}

	public JComponent add(Icon image, String text, String tooltip) {
		JComponent comp = super.add(image, text, tooltip);
		return comp;
	}

}