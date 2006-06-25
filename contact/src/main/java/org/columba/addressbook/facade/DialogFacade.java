package org.columba.addressbook.facade;

import java.net.URI;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.gui.dialog.contact.ContactEditorDialog;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.api.gui.frame.IContainer;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.dialog.ErrorDialog;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.logging.Logging;

public class DialogFacade implements IDialogFacade {

	/**
	 * @see org.columba.addressbook.facade.IDialogFacade#openContactDialog(java.net.URI)
	 */
	public void openContactDialog(URI location) {
		// example: "columba://org.columba.contact/<folder-id>/<contact-id>"
		String s = location.toString();

		// TODO: @author fdietz replace with regular expression
		int index = s.lastIndexOf('/');
		String contactId = s.substring(index + 1, s.length());
		String folderId = s.substring(s.lastIndexOf('/', index - 1) + 1, index);

		IContainer[] container = FrameManager.getInstance().getOpenFrames();
		if (container == null || container.length == 0)
			throw new RuntimeException("No frames available");

		IFrameMediator frameMediator = container[0].getFrameMediator();

		IContactFolder folder = (IContactFolder) AddressbookTreeModel
				.getInstance().getFolder(folderId);

		IContactModel card = null;
		try {
			card = (IContactModel) folder.get(contactId);
		} catch (Exception e) {
			if (Logging.DEBUG)
				e.printStackTrace();
			ErrorDialog.createDialog(e.getMessage(), e);
		}

		// 
		ContactEditorDialog dialog = new ContactEditorDialog(
				frameMediator.getView().getFrame(), card);

		if (dialog.getResult()) {

			try {
				// modify card properties in folder
				folder.modify(contactId, dialog.getDestModel());
			} catch (Exception e1) {
				if (Logging.DEBUG)
					e1.printStackTrace();

				ErrorDialog.createDialog(e1.getMessage(), e1);
			}

		}
	}
}
