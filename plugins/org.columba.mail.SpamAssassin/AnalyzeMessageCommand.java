import java.io.InputStream;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;

/**
 * @author fdietz
 * 
 * let spamassassin go through all messages: - analyze message - tag as
 * spam/ham in adding two more headerfields
 * 
 * added headerfields are: X-Spam-Level: digit number columba.spam: true/false
 * (create a filter on this headerfield)
 *  
 */
public class AnalyzeMessageCommand extends FolderCommand {

	Folder srcFolder;
	protected FolderCommandAdapter adapter;

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

	public void updateGUI() throws Exception {

		// get source references
		FolderCommandReference[] r = adapter.getSourceFolderReferences();

		// for every source references
		TableModelChangedEvent ev;
		for (int i = 0; i < r.length; i++) {

			// update table
			ev =
				new TableModelChangedEvent(
					TableModelChangedEvent.MARK,
					r[i].getFolder(),
					r[i].getUids(),
					r[i].getMarkVariant());

			TableUpdater.tableChanged(ev);

			// update treemodel
			MailInterface.treeModel.nodeChanged(r[i].getFolder());
		}

		// get update reference
		// -> only available if VirtualFolder is involved in operation
		FolderCommandReference u = adapter.getUpdateReferences();
		if (u != null) {

			ev =
				new TableModelChangedEvent(
					TableModelChangedEvent.MARK,
					u.getFolder(),
					u.getUids(),
					u.getMarkVariant());

			TableUpdater.tableChanged(ev);
			MailInterface.treeModel.nodeChanged(u.getFolder());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
		adapter = new FolderCommandAdapter(r);

		// this could also happen while using a virtual folder
		// -> loop through all available source references
		for (int j = 0; j < adapter.getSourceFolderReferences().length; j++) {
			srcFolder =
				(Folder) adapter.getSourceFolderReferences()[j].getFolder();
			Object[] uids = adapter.getSourceFolderReferences()[j].getUids();
			worker.setDisplayText(
				"Applying analyzer to " + srcFolder.getName() + "...");

			worker.setProgressBarMaximum(uids.length);
			for (int i = 0; i < uids.length; i++) {
				AnalyzeMessageCommand.addHeader(srcFolder, uids[i], worker);
				worker.setProgressBarValue(i);
			}

		}

	}

	public static void addHeader(Folder srcFolder, Object uid, Worker worker)
		throws Exception {
		//Header header = srcFolder.getHeaderFields(uid, new String[]
		// {"X-Spam-Level"} );
		InputStream rawMessageSource = srcFolder.getMessageSourceStream(uid);
		IPCHelper ipcHelper = new IPCHelper();

		//String cmd = "spamassassin -L";

		String cmd = ExternalToolsHelper.getSpamc() + " -c";

		String result = null;
		int exitVal = -1;
		try {
			ColumbaLogger.log.debug("creating process..");

			ipcHelper.executeCommand(cmd);

			ColumbaLogger.log.debug("sending to stdin..");

			ipcHelper.send(rawMessageSource);

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

		//header.set("X-Spam-Level", result);

		if (exitVal == 1)
			// spam found

			srcFolder.setAttribute(uid, "columba.spam", Boolean.TRUE);
		else
			srcFolder.setAttribute(uid, "columba.spam", Boolean.FALSE);

		result = null;

	}

}
