/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.FrameModel;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenNewAddressbookWindowAction extends FrameAction {

	public OpenNewAddressbookWindowAction(AbstractFrameController controller) {
			super(
				controller,
				"Addressbook",
				"Addressbook",
				"OPEN_NEW_ADDRESSBOOK_WINDOW",
				null,
				null,
				' ',
				null);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent evt) {
			FrameModel.openView("Addressbook");
			//getFrameController().getModel().openView();
		}
}
