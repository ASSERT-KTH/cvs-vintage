//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.columba.core.action.BasicAction;
import org.columba.mail.gui.message.MessageController;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class MessageActionListener implements ActionListener
{
	private MessageController messageController;

	public BasicAction dictAction;

	public MessageActionListener( MessageController messageController )
	{
		this.messageController = messageController;

		dictAction =
			new BasicAction(
				"Dict.org lookup selection...", "Dict lookup selection...", "Look up definition of selection with online dictionary...", "DICT",
				null,
				null,
				'0',
				null);
		dictAction.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();

		/*
		if (action
			.equals(MainInterface.frameController.globalActionCollection.copyAction.getActionCommand()))
		{
			copy();
		}
		else if (
			action.equals(
				MainInterface.frameController.globalActionCollection.selectAllAction.getActionCommand()))
		{
			selectAll();
		}
		*/
		
		if ( action.equals( dictAction.getActionCommand() ) )
		{
			// FIXME
			/*
			String text = messageController.getView().getSelectedText();


			DictLookup dict = DictLookup.getInstance();
			dict.lookup( text );
			*/
		}

	}

	public void copy()
	{
		// FIXME
		/*
		JTextComponent c =
			(JTextComponent) messageController.getView().getActiveViewer();

		c.copy();
		*/
	}

	public void selectAll()
	{
		// FIXME
		/*
		JTextComponent c =
			(JTextComponent) messageController.getView().getActiveViewer();

		c.selectAll();
		*/
	}

}
