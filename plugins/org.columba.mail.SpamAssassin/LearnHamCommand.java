import java.util.logging.Logger;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;


/**
 * @author fdietz
 *
 * command:
 *
 *  sa-learn --ham
 *
 * command description:
 *
 * Learn the input message(s) as ham. If you have previously learnt any
 * of the messages as spam, SpamAssassin will forget them first, then
 * re-learn them as ham. Alternatively, if you have previously learnt
 * them as ham, it'll skip them this time around.
 *
 */
public class LearnHamCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getAnonymousLogger();

    /**

             * @param references
             */
    public LearnHamCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public LearnHamCommand(FrameMediator frame,
        DefaultCommandReference[] references) {
        super(frame, references);
    }

    /* (non-Javadoc)
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
        FolderCommandAdapter adapter = new FolderCommandAdapter(r);

        // there can be only one reference for this command
        MessageFolder srcFolder = (MessageFolder) adapter.getSourceFolderReferences()[0].getFolder();

        worker.setDisplayText("Learning ham from " + srcFolder.getName() +
            "...");

        IPCHelper ipcHelper = new IPCHelper();

        String path = srcFolder.getDirectoryFile().getAbsolutePath();

        LOG.info("creating process..");
        ipcHelper.executeCommand(ExternalToolsHelper.getSALearn() +
            " --ham --dir " + path);

        int exitCode = ipcHelper.waitFor();
        LOG.info("exitcode=" + exitCode);

        String output = ipcHelper.getOutputString();
        LOG.info("retrieving output: " + output);

        worker.setDisplayText("SpamAssassin: " + output);

        LOG.info("wait for threads to join..");
        ipcHelper.waitForThreads();
    }
}
