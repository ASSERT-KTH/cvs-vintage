package org.columba.core.command;

import java.util.Vector;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CompoundCommand extends Command {
	protected Vector commandList;
	protected Vector referenceList;

	/**
	 * Constructor for CompoundCommand.
	 * Caution : Never use this command with Virtual Folders!
	 * @param frameController
	 * @param references
	 */
	public CompoundCommand() {
		super(null, null);
		commandList = new Vector();
		referenceList = new Vector();

		priority = Command.NORMAL_PRIORITY;
		commandType = Command.NORMAL_OPERATION;
	}

	public void add(Command c) {
		commandList.add(c);

		FolderCommandReference[] commandRefs =
			(FolderCommandReference[]) c.getReferences();
		for (int i = 0; i < commandRefs.length; i++) {
			if (!referenceList.contains(commandRefs[i].getFolder()))
				referenceList.add(commandRefs[i].getFolder());
		}
	}
	
	public void finish() throws Exception {
		Command c;
		for (int i = 0; i < commandList.size(); i++) {
			c = (Command) commandList.get(i);
			c.finish();
		}
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		Command c;
		for (int i = 0; i < commandList.size(); i++) {
			c = (Command) commandList.get(i);
			c.execute(worker);
		}
	}

	//	/**
	//	 * @see org.columba.core.command.Command#canBeProcessed(int)
	//	 */
	//	public boolean canBeProcessed(int operationMode) {
	//
	//		boolean result = true;
	//		Command c;
	//		for (int i = 0; i < commandList.size(); i++) {
	//			c = (Command) commandList.get(i);
	//			result &= c.canBeProcessed(operationMode);
	//		}
	//
	//		if (!result) {
	//
	//			releaseAllFolderLocks(operationMode);
	//		}
	//
	//		return result;
	//	}
	//
	//	/**
	//	 * @see org.columba.core.command.Command#releaseAllFolderLocks(int)
	//	 */
	//	public void releaseAllFolderLocks(int operationMode) {
	//		Command c;
	//		for (int i = 0; i < commandList.size(); i++) {
	//			c = (Command) commandList.get(i);
	//			c.releaseAllFolderLocks(operationMode);
	//		}
	//	}

	/**
	 * @see org.columba.core.command.Command#getReferences()
	 */
	public DefaultCommandReference[] getReferences() {
		FolderCommandReference[] refs =
			new FolderCommandReference[referenceList.size()];

		for (int i = 0; i < referenceList.size(); i++) {
			

			refs[i] =
				new FolderCommandReference(
					(FolderTreeNode) referenceList.get(i));

		}

		return refs;
	}

}
