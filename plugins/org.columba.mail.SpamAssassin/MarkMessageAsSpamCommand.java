
import java.io.InputStream;
import java.util.logging.Logger;

import org.columba.core.command.Command;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.folder.IMailbox;

/**
 * @author fdietz
 * 
 * 
 *  
 */
public class MarkMessageAsSpamCommand extends Command {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger.getAnonymousLogger();

	IMailbox srcFolder;

	/**
	 * @param references
	 */
	public MarkMessageAsSpamCommand(ICommandReference references) {
		super(references);
	}

	/**
	 * @param frame
	 * @param references
	 */
	public MarkMessageAsSpamCommand(FrameMediator frame,
			ICommandReference references) {
		super(frame, references);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		IFolderCommandReference r = (IFolderCommandReference) getReference();

		srcFolder = (IMailbox) r.getSourceFolder();

		Object[] uids = r.getUids();
		worker.setDisplayText("Applying analyzer to " + srcFolder.getName()
				+ "...");

		worker.setProgressBarMaximum(uids.length);

		for (int i = 0; i < uids.length; i++) {
			markMessage(srcFolder, uids[i], worker);
			worker.setProgressBarValue(i);
		}

	}

	public static void markMessage(IMailbox srcFolder, Object uid,
			WorkerStatusController worker) throws Exception {
		InputStream rawMessageSource = srcFolder.getMessageSourceStream(uid);

		IPCHelper ipcHelper = new IPCHelper();

		LOG.info("creating process..");
		ipcHelper.executeCommand(ExternalToolsHelper.getSALearn()
				+ " --no-rebuild --spam --single");

		LOG.info("sending to stdin..");

		ipcHelper.send(rawMessageSource);

		int exitVal = ipcHelper.waitFor();

		LOG.info("exitcode=" + exitVal);

		LOG.info("retrieving output..");

		String result = ipcHelper.getOutputString();

		LOG.info("output=" + result);

		ipcHelper.waitForThreads();

		srcFolder.setAttribute(uid, "columba.spam", Boolean.TRUE);
	}
}