import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaHeader;

/**
 * @author fdietz
 *
 * let spamassassin go through all messages:
 * - analyze message
 * - tag as spam/ham in adding two more headerfields
 * 
 * added headerfields are:
 *  X-Spam-Level: digit number
 *  X-Spam-Flag: YES/NO (create a filter on this headerfield)
 * 
 */
public class AnalyzeMessageCommand extends FolderCommand {

	Folder srcFolder;

	/**
	
		 * @param references
		 */
	public AnalyzeMessageCommand(DefaultCommandReference[] references) {
		super(references);

	}
	/**
	 * @param frame
	 * @param references
	 */
	public AnalyzeMessageCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
		FolderCommandAdapter adapter = new FolderCommandAdapter(r);

		// this could also happen while using a virtual folder
		// -> loop through all available source references
		for (int j = 0; j < adapter.getSourceFolderReferences().length; j++) {
			srcFolder =
				(Folder) adapter.getSourceFolderReferences()[j].getFolder();
			Object[] uids = adapter.getSourceFolderReferences()[j].getUids();
			worker.setDisplayText(
				"Applying analyzer to" + srcFolder.getName() + "...");

			for (int i = 0; i < uids.length; i++) {
				AnalyzeMessageCommand.addHeader(srcFolder, uids[i], worker);
			}
		}

	}

	public static void addHeader(Folder srcFolder, Object uid, Worker worker)
		throws Exception {
		ColumbaHeader header = srcFolder.getMessageHeader(uid, worker);
		String rawString = srcFolder.getMessageSource(uid, worker);

		IPCHelper ipcHelper = new IPCHelper();

		//String cmd = "spamassassin -L";
		String cmd = "spamc -c";

		String result = null;
		int exitVal = -1;
		try {
			ColumbaLogger.log.debug("creating process..");

			ipcHelper.executeCommand(cmd);

			ColumbaLogger.log.debug("sending to stdin..");

			ipcHelper.send(rawString);

			exitVal = ipcHelper.waitFor();

			ColumbaLogger.log.debug("exitcode=" + exitVal);

			ColumbaLogger.log.debug("retrieving output..");
			result = ipcHelper.getOutputString();

			ipcHelper.waitForThreads();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (result == null)
			return;

		header.set("X-Spam-Level", result);

		if (exitVal == 1)
			// spam found

			header.set("X-Spam-Flag", "YES");
		else
			header.set("X-Spam-Flag", "NO");

		// free memory
		rawString = null;
		result = null;

	}

}
