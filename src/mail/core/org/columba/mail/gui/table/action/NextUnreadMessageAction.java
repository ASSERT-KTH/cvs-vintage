/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NextUnreadMessageAction
	extends FrameAction
	implements SelectionListener {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public NextUnreadMessageAction(AbstractFrameController frameController) {
		super(
			frameController,
		MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_nextunreadmessage"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_nextunreadmessage_tooltip"),
			"NEXT_UNREAD_MESSAGE",
			null,
			null,
			'U',
			KeyStroke.getKeyStroke("N"));
		setEnabled(false);
		((MailFrameController) frameController).registerTableSelectionListener(
			this);
	}

	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((MailFrameController) getFrameController()).getTableSelection();

		if (r.length > 0) {
			FolderCommandReference ref = r[0];
			TableController table =
				((MailFrameController) getFrameController()).tableController;
			MessageNode node = table.getView().getSelectedNode();
			if ( node == null ) return;
			
			MessageNode nextNode = (MessageNode) node.getNextNode();
			if ( nextNode == null ) return;
			
			ColumbaHeader header = (ColumbaHeader) nextNode.getHeader();

			boolean seen = header.getFlags().getSeen();
			while (seen==true) {
				// try next message	
				nextNode = (MessageNode) nextNode.getNextNode();
				if ( nextNode == null )return;
				
				header = (ColumbaHeader) nextNode.getHeader();
				seen = header.getFlags().getSeen();
				
				
			}

			if (nextNode == null)
				return;

			Object nextUid = nextNode.getUid();

			Object[] uids = new Object[1];
			uids[0] = nextUid;
			ref.setUids(uids);

			((MailFrameController) getFrameController()).setTableSelection(r);
			table.setSelected(uids);
			
			MainInterface.processor.addOp(
				new ViewMessageCommand(getFrameController(), r));
		}
	}
	/* (non-Javadoc)
			 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
			 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
