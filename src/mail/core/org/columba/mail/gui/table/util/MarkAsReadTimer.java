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
package org.columba.mail.gui.table.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.table.TableController;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class MarkAsReadTimer implements ActionListener {
	private Timer timer;

	private final static int ONE_SECOND = 1000;

	private int value;
	private int maxValue;

	private FolderCommandReference message;

	private TableController tableController;

	public MarkAsReadTimer(TableController tableController) {

		this.tableController = tableController;

		XmlElement markasread =
		MailConfig.get("options").getElement("/options/markasread");

		String delay = markasread.getAttribute("delay", "2");
		this.maxValue = Integer.parseInt(delay);
		
		timer = new Timer(ONE_SECOND * maxValue, this);

	}

	public void setMaxValue(int i) {
		maxValue = i;
		
		timer = new Timer(ONE_SECOND * maxValue, this);
	}

	public synchronized void stopTimer() {
		value = 0;

		ColumbaLogger.log.debug("MarkAsRead-timer stopped");

		timer.stop();
	}

	public synchronized void restart(FolderCommandReference reference) {

		ColumbaLogger.log.debug("MarkAsRead-timer started");
		
		message = reference;
		value = 0;
		timer.restart();
	}

	public void actionPerformed(ActionEvent e) {

		timer.stop();

		FolderCommandReference[] r = new FolderCommandReference[]{ message };
		
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_READ);

		MarkMessageCommand c = new MarkMessageCommand(r);

		MainInterface.processor.addOp(c);

		value++;
	}
}