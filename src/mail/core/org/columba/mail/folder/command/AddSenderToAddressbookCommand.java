package org.columba.mail.folder.command;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.parser.AddressParser;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.FrameController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AddSenderToAddressbookCommand extends FolderCommand {

	org.columba.addressbook.folder.Folder selectedFolder;

	/**
	 * Constructor for AddSenderToAddressbookCommand.
	 * @param references
	 */
	public AddSenderToAddressbookCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * Constructor for AddSenderToAddressbookCommand.
	 * @param frame
	 * @param references
	 */
	public AddSenderToAddressbookCommand(
		FrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);
	}

	/**
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		Object[] uids = r[0].getUids();
		Folder folder = (Folder) r[0].getFolder();

		SelectAddressbookFolderDialog dialog =
			MainInterface
				.addressbookInterface
				.tree
				.getSelectAddressbookFolderDialog();

		selectedFolder = dialog.getSelectedFolder();

		if (selectedFolder == null)
			return;

		for (int i = 0; i < uids.length; i++) {

			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			String sender = (String) header.get("From");

			addSender(sender);

		}
	}

	public void addSender(String sender) {
		if (sender == null)
			return;

		if (sender.length() > 0) {
			

			String address = AddressParser.getAddress(sender);
			System.out.println("address:" + address);

			if (!selectedFolder.exists(address)) {
				ContactCard card = new ContactCard();

				String fn = AddressParser.getDisplayname(sender);
				System.out.println("fn=" + fn);

				card.set("fn", fn);
				card.set("displayname", fn);
				card.set("email", "internet", address);

				selectedFolder.add(card);
			}
		}
	}

}
