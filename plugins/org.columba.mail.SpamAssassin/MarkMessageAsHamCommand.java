import java.io.InputStream;
import java.util.logging.Logger;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;


/**
 * @author fdietz
 *
 *
 *
 */
public class MarkMessageAsHamCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getAnonymousLogger();

    MessageFolder srcFolder;
    protected FolderCommandAdapter adapter;

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
    public MarkMessageAsHamCommand(FrameMediator frame,
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
            ev = new TableModelChangedEvent(TableModelChangedEvent.MARK,
                    r[i].getFolder(), r[i].getUids(), r[i].getMarkVariant());

            TableUpdater.tableChanged(ev);

            // update treemodel
            MailInterface.treeModel.nodeChanged(r[i].getFolder());
        }

        // get update reference
        // -> only available if VirtualFolder is involved in operation
        FolderCommandReference u = adapter.getUpdateReferences();

        if (u != null) {
            ev = new TableModelChangedEvent(TableModelChangedEvent.MARK,
                    u.getFolder(), u.getUids(), u.getMarkVariant());

            TableUpdater.tableChanged(ev);
            MailInterface.treeModel.nodeChanged(u.getFolder());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
        adapter = new FolderCommandAdapter(r);

        // this could also happen while using a virtual folder
        // -> loop through all available source references
        for (int j = 0; j < adapter.getSourceFolderReferences().length; j++) {
            srcFolder = (MessageFolder) adapter.getSourceFolderReferences()[j].getFolder();

            Object[] uids = adapter.getSourceFolderReferences()[j].getUids();
            worker.setDisplayText("Applying analyzer to " +
                srcFolder.getName() + "...");

            worker.setProgressBarMaximum(uids.length);

            for (int i = 0; i < uids.length; i++) {
                markMessage(srcFolder, uids[i], worker);
                worker.setProgressBarValue(i);
            }
        }
    }

    public static void markMessage(MessageFolder srcFolder, Object uid, WorkerStatusController worker)
        throws Exception {
        InputStream rawMessageSource = srcFolder.getMessageSourceStream(uid);

        IPCHelper ipcHelper = new IPCHelper();

        LOG.info("creating process..");
        ipcHelper.executeCommand(ExternalToolsHelper.getSALearn() +
            " --no-rebuild --ham --single");

        LOG.info("sending to stdin..");

        ipcHelper.send(rawMessageSource);

        int exitVal = ipcHelper.waitFor();

        LOG.info("exitcode=" + exitVal);

        LOG.info("retrieving output..");

        String result = ipcHelper.getOutputString();

        LOG.info("output=" + result);

        ipcHelper.waitForThreads();

        srcFolder.setAttribute(uid, "columba.spam", Boolean.FALSE);
    }
}
