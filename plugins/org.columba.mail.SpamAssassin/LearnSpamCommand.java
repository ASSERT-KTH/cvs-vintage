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
 *  sa-learn --spam
 *
 * command description:
 *
 * Learn the input message(s) as spam. If you have previously learnt any of
 * the messages as ham, SpamAssassin will forget them first, then re-learn
 * them as spam. Alternatively, if you have previously learnt them as spam,
 * it'll skip them this time around.
 *
 */
public class LearnSpamCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getAnonymousLogger();

    /**

             * @param references
             */
    public LearnSpamCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public LearnSpamCommand(FrameMediator frame,
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

        worker.setDisplayText("Learning spam from " + srcFolder.getName() +
            "...");

        IPCHelper ipcHelper = new IPCHelper();

        String path = srcFolder.getDirectoryFile().getAbsolutePath();

        LOG.info("creating process..");
        ipcHelper.executeCommand(ExternalToolsHelper.getSALearn() +
            " --spam --dir " + path);

        int exitCode = ipcHelper.waitFor();
        LOG.info("exitcode=" + exitCode);

        String output = ipcHelper.getOutputString();
        LOG.info("retrieving output: " + output);

        worker.setDisplayText("SpamAssassin: " + output);

        LOG.info("wait for threads to join..");
        ipcHelper.waitForThreads();
    }
}
