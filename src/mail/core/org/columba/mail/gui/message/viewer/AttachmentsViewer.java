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

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.AttachmentModel;
import org.columba.mail.gui.message.attachment.util.AttachmentImageIconLoader;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.StreamableMimePart;
import org.frapuccino.iconpanel.IconPanel;

/**
 * @author fdietz
 *  
 */
public class AttachmentsViewer extends IconPanel implements Viewer {

	private AttachmentModel model;
	private MimeTree mimePartTree;
	
	public AttachmentsViewer(AttachmentModel model) {
		super();
		
		this.model = model;
		
		setOpaque(true);
		setBackground(UIManager.getColor("List.background"));
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

			//Get Text for Icon
			if (header.getFileName() != null) {
				text = header.getFileName();
			} else {
				text = contentType + "/" + contentSubtype;
			}

			//Get Tooltip for Icon
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

			icon = AttachmentImageIconLoader.getImageIcon(type.getType(), type
					.getSubtype());

			add(icon, text, tooltip.toString());
			output = true;
		}

		return output;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#view(org.columba.mail.folder.IMailbox, java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator) throws Exception {
		mimePartTree = folder.getMimePartTree(uid);
		
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		setMimePartTree(mimePartTree);	
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#getView()
	 */
	public JComponent getView() {
		// TODO Auto-generated method stub
		return null;
	}
}