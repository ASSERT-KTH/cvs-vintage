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
 * let spamassassin go through all messages:
 * - analyze message
 * - tag as spam/ham in adding two more headerfields
 *
 * added headerfields are:
 *  X-Spam-Level: digit number
 *  X-Spam-Flag: YES/NO (create a filter on this headerfield)
 *
 */
public class AnalyzeFolderCommand extends FolderCommand {
    MessageFolder srcFolder;

    /**

             * @param references
             */
    public AnalyzeFolderCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public AnalyzeFolderCommand(FrameMediator frame,
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
        srcFolder = (MessageFolder) adapter.getSourceFolderReferences()[0].getFolder();

        worker.setDisplayText("Applying analyzer to " + srcFolder.getName() +
            "...");

        Object[] uids = srcFolder.getUids();

        for (int i = 0; i < uids.length; i++) {
            if (worker.cancelled()) {
                return;
            }

            AnalyzeMessageCommand.addHeader(srcFolder, uids[i], worker);
        }
    }
}
