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
 
 * 
 */
public class MarkMessageAsHamCommand extends FolderCommand {

	Folder srcFolder;

	/**
	
		 * @param references
		 */
	public MarkMessageAsHamCommand(DefaultCommandReference[] references) {
		super(references);

	}
	/**
	 * @param frame
	 * @param references
	 */
	public MarkMessageAsHamCommand(
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
				markMessage(srcFolder, uids[i], worker);
			}
		}

	}

	public static void markMessage(Folder srcFolder, Object uid, Worker worker)
		throws Exception {
		ColumbaHeader header = srcFolder.getMessageHeader(uid, worker);
		String rawString = srcFolder.getMessageSource(uid, worker);

		IPCHelper ipcHelper = new IPCHelper();

		ColumbaLogger.log.debug("creating process..");
		ipcHelper.executeCommand("sa-learn --no-rebuild --ham --single");

		ColumbaLogger.log.debug("sending to stdin..");

		ipcHelper.send(rawString);

		int exitVal = ipcHelper.waitFor();

		ColumbaLogger.log.debug("exitcode=" + exitVal);
		
		ColumbaLogger.log.debug("retrieving output..");
		String result = ipcHelper.getOutputString();
		
		ColumbaLogger.log.debug("output="+result);
		
		ipcHelper.waitForThreads();

		header.set("X-Spam-Flag", "YES");

	}

}
