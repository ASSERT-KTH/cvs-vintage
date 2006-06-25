package org.columba.mail.facade;

import java.net.URI;

import org.columba.api.gui.frame.IContainer;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.plugin.PluginLoadingFailedException;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.frame.DefaultContainer;
import org.columba.core.gui.frame.FrameManager;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.messageframe.MessageFrameController;
import org.columba.mail.gui.tree.FolderTreeModel;

public class DialogFacade implements IDialogFacade {


	public DialogFacade() {
		super();
	}

	
    /**
     * @see org.columba.mail.facade.IDialogFacade#openComposer()
     */
    public void openComposer() {
        // Choice btw. text and html will be based on stored option
        try {
        	FrameManager.getInstance().openView("Composer");
        } catch (PluginLoadingFailedException plfe) {} //should not occur
    }
    
	/**
	 * @see org.columba.mail.facade.IDialogFacade#openMessage(java.net.URI)
	 */
	public void openMessage(URI location) {
		// example: "columba://org.columba.mail/<folder-id>/<message-id>"
		String s = location.toString();

		// TODO: @author fdietz replace with regular expression
		int index = s.lastIndexOf('/');
		String messageId = s.substring(index+1, s.length());
		String folderId = s.substring(s.lastIndexOf('/', index-1)+1, index);

		int folderIdIntValue = Integer.parseInt(folderId);

		IContainer[] container = FrameManager.getInstance().getOpenFrames();
		if (container == null || container.length == 0)
			throw new RuntimeException("No frames available");

		IFrameMediator mailFrameMediator = null;
		for (int i = 0; i < container.length; i++) {
			IFrameMediator mediator = container[i].getFrameMediator();
			if (mediator.getId().equals("ThreePaneMail")) {
				// found mail component frame
				mailFrameMediator = mediator;
			}
		}

		if (mailFrameMediator == null)
			throw new RuntimeException("No mail frame mediator found");

		// type-cast here is safe
		MessageFrameController c = new MessageFrameController(
				(ThreePaneMailFrameController) mailFrameMediator);
		new DefaultContainer(c);

		IMailbox folder = (IMailbox) FolderTreeModel.getInstance().getFolder(
				folderIdIntValue);
		IMailFolderCommandReference r = new MailFolderCommandReference(folder,
				new Object[] { messageId });

		c.setTreeSelection(r);

		c.setTableSelection(r);

		CommandProcessor.getInstance().addOp(new ViewMessageCommand(c, r));
	}
	
}
