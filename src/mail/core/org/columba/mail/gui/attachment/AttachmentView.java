package org.columba.mail.gui.attachment;

import java.util.LinkedList;

import javax.swing.ImageIcon;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.action.BasicAction;
import org.columba.mail.gui.attachment.action.AttachmentActionListener;
import org.columba.mail.gui.attachment.util.AttachmentImageIconLoader;
import org.columba.mail.gui.attachment.util.IconPanel;
import org.columba.mail.gui.frame.MailMenu;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentView extends IconPanel {

	private LinkedList displayedMimeParts;

	private AttachmentModel model;

	

	public AttachmentView(AttachmentModel model) {
		super();

		this.model = model;
	}

	public AttachmentModel getModel() {
		return model;
	}

	
	public MimePart getSelectedMimePart() {
		return (MimePart) model.getDisplayedMimeParts().get(getSelected());
	}

	public boolean setMimePartTree(MimePartTree collection) {
		String contentType;
		String contentSubtype;
		String text = new String();
		boolean output = false;

		removeAll();

		model.setCollection(collection);

		LinkedList displayedMimeParts = model.getDisplayedMimeParts();

		// Display resulting MimeParts

		for (int i = 0; i < displayedMimeParts.size(); i++) {
			MimePart mp = (MimePart) displayedMimeParts.get(i);

			contentType = mp.getHeader().contentType;
			contentSubtype = mp.getHeader().contentSubtype;

			if (mp.getHeader().getFileName() != null) {
				text =
					mp.getHeader().getFileName()
						+ " ("
						+ contentType
						+ "/"
						+ contentSubtype
						+ ")";
			} else {
				text = contentType + "/" + contentSubtype;
			}

			ImageIcon icon = null;

			icon =
				AttachmentImageIconLoader.getImageIcon(
					mp.getHeader().contentType,
					mp.getHeader().contentSubtype);

			add(icon, text);
			output = true;
		}

		return output;
	}

}
