/*
 * Created on Jun 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.JAbstractAction;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.TableOwnerInterface;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.TableController;

/**
 * @author waffel
 *
 * The downAction is the action when you pressing the down key (not on NUM-PAD). 
 * If you do so, the nextMessage down your key is selected and shown in the
 * message-view. If no more message down your key, then nothing changed.
 */
public class DownAction extends JAbstractAction implements SelectionListener {
	TableController tableController;
	AbstractMailFrameController frameController;

	public DownAction(AbstractMailFrameController frameController) {
		super();
		this.tableController =
			((TableOwnerInterface) frameController).getTableController();
		this.frameController = frameController;
		(
			(
				AbstractMailFrameController) frameController)
					.registerTableSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		ColumbaLogger.log.debug("action down performed");

		int selectedRowCount = tableController.getView().getSelectedRowCount();
		int row = tableController.getView().getSelectedRow();

		row = row + 1;

		tableController.getView().setRowSelectionInterval(row, row);
		tableController.getView().scrollRectToVisible(
			tableController.getView().getCellRect(row, 0, false));

		/*
		// getting last selection
		FolderCommandReference[] r = frameController.getTableSelection();
		FolderCommandReference ref = r[0];
		ColumbaLogger.log.debug("folderCommandRef: " + ref);
		// getting current uid
		Object[] uids = ref.getUids();
		ColumbaLogger.log.debug("curr uids: " + uids);
		// getting current node (under the selection)
		DefaultMutableTreeNode currNode =
			tableController.getView().getMessagNode(uids[0]);
		ColumbaLogger.log.debug("currNode: " + currNode);
		// getting next node
		DefaultMutableTreeNode nextNode = currNode.getNextNode();
		// if next node is null (the end of the list) return
		if (nextNode == null) {
			return;
		}
		ColumbaLogger.log.debug("nextNode: " + nextNode);
		// getting from the next node the uid
		Object[] nextUids = new Object[1];
		nextUids[0] = ((MessageNode) nextNode).getUid();
		ColumbaLogger.log.debug("prevUids: " + nextUids);
		// and set this to the actual ref
		ref.setUids(nextUids);
		
		// check if the node is not null
		MessageNode[] nodes = new MessageNode[nextUids.length];
		for (int i = 0; i < nextUids.length; i++) {
			nodes[i] = tableController.getHeaderTableModel().getMessageNode(nextUids[i]);
		}
		boolean node_ok = true;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i]== null) {
				node_ok = false;
				break;
			}
		}
		// if the node is not null
		if (node_ok) {
			// select it
			tableController.setSelected(nextUids);
			
			int row = tableController.getView().getSelectedRow();
			tableController.getView().scrollRectToVisible(tableController.getView().getCellRect(row,0,false));
			
		
			FolderCommandReference[] refNew = new FolderCommandReference[1];
			refNew[0] = new FolderCommandReference(ref.getFolder(), nextUids);
			// view the message under the new node
			MainInterface.processor.addOp(
			new ViewMessageCommand(frameController, refNew));
		}
		*/
	}
	/* (non-Javadoc)
	 * @see org.columba.core.gui.selection.SelectionListener#selectionChanged(org.columba.core.gui.selection.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		FolderCommandReference[] r = frameController.getTableSelection();

		MainInterface.processor.addOp(
			new ViewMessageCommand(frameController, r));

	}

}
