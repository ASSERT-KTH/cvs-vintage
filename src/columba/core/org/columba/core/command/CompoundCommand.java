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

package org.columba.core.command;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.virtual.VirtualFolder;

/**
 * Special type of {@link Command}which is used for a set of different
 * commands.
 * <p>
 * This is used by {@link FilterAction}and {@link VirtualFolder}to execute
 * commands, which work on a set of references.
 * 
 * @author tstich, fdietz
 */
public class CompoundCommand extends Command {
	protected List commandList;

	/**
	 * Constructor for CompoundCommand. Caution : Never use this command with
	 * Virtual Folders!
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public CompoundCommand() {
		super(null, null);
		commandList = new Vector();

		priority = Command.NORMAL_PRIORITY;
		commandType = Command.NORMAL_OPERATION;
	}

	public void add(Command c) {
		commandList.add(c);

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		Command c;

		for (Iterator it = commandList.iterator(); it.hasNext();) {
			c = (Command) it.next();
			c.execute(worker);
		}
	}

	/**
	 * @see org.columba.core.command.Command#canBeProcessed()
	 */
	public boolean canBeProcessed() {
		boolean result = true;
		Command c;
		for (Iterator it = commandList.iterator(); it.hasNext();) {
			c = (Command) it.next();
			result &= c.canBeProcessed();
		}

		if (!result) {

			releaseAllFolderLocks();
		}

		return result;
	}

	/**
	 * @see org.columba.core.command.Command#releaseAllFolderLocks()
	 */
	public void releaseAllFolderLocks() {
		Command c;
		for (Iterator it = commandList.iterator(); it.hasNext();) {
			c = (Command) it.next();
			c.releaseAllFolderLocks();
		}
	}

	
	/**
	 * 
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		Command c;

		for (Iterator it = commandList.iterator(); it.hasNext();) {
			c = (Command) it.next();

			c.updateGUI();
		}
	}
}