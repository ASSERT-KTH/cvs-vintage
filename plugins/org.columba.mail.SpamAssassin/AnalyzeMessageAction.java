import java.awt.event.ActionEvent;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.command.CommandProcessor;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;


/**
 * @author fdietz
 *
 *
 */
public class AnalyzeMessageAction extends AbstractColumbaAction
    implements ISelectionListener {
    /**
     * @param frameMediator
     * @param name
     * @param longDescription
     * @param actionCommand
     * @param small_icon
     * @param big_icon
     * @param mnemonic
     * @param keyStroke
     */
    public AnalyzeMessageAction(FrameMediator frameMediator) {
        super(frameMediator, "Analyze Messages");

        // tooltip text
        putValue(SHORT_DESCRIPTION, "Analyze selected messages");

        setEnabled(false);

        ((MailFrameMediator) frameMediator).registerTableSelectionListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent e) {
        if (((TableSelectionChangedEvent) e).getUids().length > 0) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        IFolderCommandReference r = ((AbstractMailFrameController) getFrameMediator()).getTableSelection();

        CommandProcessor.getInstance().addOp(new AnalyzeMessageCommand(r));
    }
}
