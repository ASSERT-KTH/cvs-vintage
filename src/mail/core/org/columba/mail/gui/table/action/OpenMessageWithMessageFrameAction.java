/*
 * Created on 08.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.messageframe.MessageFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenMessageWithMessageFrameAction
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
	public OpenMessageWithMessageFrameAction(AbstractFrameController frameController) {
		super(
			frameController,
			"Open Message in New Window",
			"Open Message in New Window",
			"OPEN_MESSAGE_IN_NEW_WINDOW",
			null,
			null,
			0,
			null);

		setEnabled(false);
		((AbstractMailFrameController) frameController).registerTableSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MessageFrameController c = new MessageFrameController();

		FolderCommandReference[] r =
			((AbstractMailFrameController) getFrameController()).getTableSelection();

		c.setTreeSelection(r);

		c.setTableSelection(r);

		/*
		c.treeController.setSelected((Folder) r[0].getFolder());
		c.setTreeSelection(r);
		
		
		c.tableController.setSelected(r[0].getUids());
				c.setTableSelection(r);
		
		MainInterface.processor.addOp(new ViewHeaderListCommand(c, r));
		*/

		MainInterface.processor.addOp(new ViewMessageCommand(c, r));

	}

	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}

}
