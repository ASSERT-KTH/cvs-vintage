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
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PreviousMessageAction
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
	public PreviousMessageAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_prevmessage"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_prevmessage_tooltip"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_prevmessage_tooltip"),
			"PREV_MESSAGE",
			null,
			ImageLoader.getSmallImageIcon("previous-message.png"),
			'M',
			KeyStroke.getKeyStroke("B"),
			false);
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
			
		if ( r.length>0 )
		{
			FolderCommandReference ref = r[0];
			TableController table = ((MailFrameController) getFrameController()).tableController;
			MessageNode node = table.getView().getSelectedNode();
			MessageNode previousNode = (MessageNode) node.getPreviousNode();
			Object nextUid = previousNode.getUid();
			
			Object[] uids = new Object[1];
			uids[0] = nextUid;
			ref.setUids(uids);
			
			((MailFrameController) getFrameController()).setTableSelection(r);
			table.setSelected(uids);
			
			MainInterface.processor.addOp(new ViewMessageCommand(getFrameController(), r));
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
